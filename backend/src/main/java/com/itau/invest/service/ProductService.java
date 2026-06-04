package com.itau.invest.service;

import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.dto.product.ProductRequest;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Gestao do catalogo de produtos de investimento. Operacoes de escrita sao
 * restritas a ADMIN (controle aplicado na camada de seguranca/controller).
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> listActive(ProductType type, Pageable pageable) {
        return type == null
                ? productRepository.findByActiveTrue(pageable)
                : productRepository.findByActiveTrueAndType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Produto", id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .type(request.type())
                .riskLevel(request.riskLevel())
                .annualRatePercent(request.annualRatePercent())
                .minimumAmount(request.minimumAmount())
                .liquidityDays(request.liquidityDays())
                .maturityDate(request.maturityDate())
                .active(true)
                .build();
        product = productRepository.save(product);
        log.info("Produto criado: productId={} name={}", product.getId(), product.getName());
        return product;
    }

    @Transactional
    public Product update(UUID id, ProductRequest request) {
        Product product = getById(id);
        product.setName(request.name());
        product.setType(request.type());
        product.setRiskLevel(request.riskLevel());
        product.setAnnualRatePercent(request.annualRatePercent());
        product.setMinimumAmount(request.minimumAmount());
        product.setLiquidityDays(request.liquidityDays());
        product.setMaturityDate(request.maturityDate());
        log.info("Produto atualizado: productId={}", id);
        return product;
    }

    /** Desativacao logica (soft delete) para preservar integridade historica. */
    @Transactional
    public void deactivate(UUID id) {
        Product product = getById(id);
        product.setActive(false);
        log.info("Produto desativado: productId={}", id);
    }
}
