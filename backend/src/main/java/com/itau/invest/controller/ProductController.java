package com.itau.invest.controller;

import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.dto.common.PageResponse;
import com.itau.invest.dto.product.ProductRequest;
import com.itau.invest.dto.product.ProductResponse;
import com.itau.invest.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "Catalogo de produtos de investimento")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Lista produtos ativos (publico), com filtro opcional por tipo")
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) ProductType type,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(
                PageResponse.of(productService.listActive(type, pageable), ProductResponse::from));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um produto pelo id (publico)")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ProductResponse.from(productService.getById(id)));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cria um novo produto (ADMIN)")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(productService.create(request)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualiza um produto existente (ADMIN)")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativa (soft delete) um produto (ADMIN)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
