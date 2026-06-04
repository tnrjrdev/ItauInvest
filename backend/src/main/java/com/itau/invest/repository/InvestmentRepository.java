package com.itau.invest.repository;

import com.itau.invest.domain.entity.Investment;
import com.itau.invest.domain.enums.InvestmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {

    @EntityGraph(attributePaths = "product")
    List<Investment> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "product")
    List<Investment> findByUserIdAndStatus(UUID userId, InvestmentStatus status);

    Optional<Investment> findByIdAndUserId(UUID id, UUID userId);

    Optional<Investment> findByUserIdAndProductIdAndStatus(
            UUID userId, UUID productId, InvestmentStatus status);
}
