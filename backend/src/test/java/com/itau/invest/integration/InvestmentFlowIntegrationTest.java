package com.itau.invest.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import com.itau.invest.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvestmentFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;

    private String registerAndGetToken() throws Exception {
        String email = "cliente-" + UUID.randomUUID() + "@itau.com.br";
        String cpf = String.format("%011d", Math.abs(email.hashCode()) % 100000000000L);
        String body = """
                {"name":"Cliente Teste","email":"%s","cpf":"%s","password":"SenhaForte@123"}
                """.formatted(email, cpf);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private UUID seedProduct() {
        Product product = Product.builder()
                .name("CDB Integracao 10% a.a.")
                .type(ProductType.CDB)
                .riskLevel(RiskLevel.BAIXO)
                .annualRatePercent(new BigDecimal("10.0000"))
                .minimumAmount(new BigDecimal("100.0000"))
                .liquidityDays(0)
                .active(true)
                .build();
        return productRepository.save(product).getId();
    }

    @Test
    void shouldRunFullInvestmentJourney() throws Exception {
        String token = registerAndGetToken();
        String auth = "Bearer " + token;
        UUID productId = seedProduct();

        // Deposito de R$ 1000
        mockMvc.perform(post("/api/v1/wallet/deposits").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000.00,\"description\":\"PIX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashBalance").value(1000.00));

        // Aplicacao de R$ 500
        MvcResult applyResult = mockMvc.perform(post("/api/v1/investments/applications")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"%s\",\"amount\":500.00}".formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.investedAmount").value(500.00))
                .andReturn();
        UUID investmentId = UUID.fromString(objectMapper
                .readTree(applyResult.getResponse().getContentAsString())
                .get("investmentId").asText());

        // Portfolio: caixa 500, investido 500
        mockMvc.perform(get("/api/v1/investments/portfolio").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashBalance").value(500.00))
                .andExpect(jsonPath("$.totalInvested").value(500.00))
                .andExpect(jsonPath("$.positions.length()").value(1));

        // Resgate parcial de R$ 200
        mockMvc.perform(post("/api/v1/investments/%s/redemptions".formatted(investmentId))
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashBalance").value(700.00));

        // Extrato deve conter os lancamentos
        MvcResult statement = mockMvc.perform(get("/api/v1/wallet/statement")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = objectMapper.readTree(statement.getResponse().getContentAsString())
                .get("content");
        assertThat(content).isNotEmpty();
    }

    @Test
    void shouldRejectUnauthenticatedAccessToWallet() throws Exception {
        mockMvc.perform(get("/api/v1/wallet"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWeakRegistration() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"a\",\"email\":\"bad\",\"cpf\":\"1\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
