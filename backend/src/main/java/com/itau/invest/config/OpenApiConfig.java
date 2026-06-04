package com.itau.invest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do OpenAPI/Swagger com esquema de autenticacao Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI itauInvestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ItauInvest API")
                        .description("Plataforma de investimentos: carteira, produtos, "
                                + "aplicacoes, resgates e posicao consolidada.")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipe ItauInvest")
                                .email("invest@itau.com.br"))
                        .license(new License().name("Proprietary")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe o token JWT obtido no login")));
    }
}
