package com.itau.invest.service;

import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import com.itau.invest.dto.product.ProductRequest;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private ProductRequest request() {
        return new ProductRequest("CDB Novo", ProductType.CDB, RiskLevel.BAIXO,
                new BigDecimal("11.0000"), new BigDecimal("100.0000"), 0, null);
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product created = productService.create(request());

        assertThat(created.getName()).isEqualTo("CDB Novo");
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void shouldGetByIdOrThrow() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateProduct() {
        UUID id = UUID.randomUUID();
        Product existing = Product.builder().name("Antigo").type(ProductType.CDB)
                .riskLevel(RiskLevel.BAIXO).annualRatePercent(new BigDecimal("9.0"))
                .minimumAmount(new BigDecimal("100")).liquidityDays(0).active(true).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        Product updated = productService.update(id, request());

        assertThat(updated.getName()).isEqualTo("CDB Novo");
        assertThat(updated.getAnnualRatePercent()).isEqualByComparingTo("11.0000");
    }

    @Test
    void shouldDeactivateProduct() {
        UUID id = UUID.randomUUID();
        Product existing = Product.builder().name("Ativo").type(ProductType.CDB)
                .riskLevel(RiskLevel.BAIXO).annualRatePercent(new BigDecimal("9.0"))
                .minimumAmount(new BigDecimal("100")).liquidityDays(0).active(true).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        productService.deactivate(id);

        assertThat(existing.isActive()).isFalse();
    }
}
