package com.itau.invest.repository;

import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndType(ProductType type, Pageable pageable);
}
