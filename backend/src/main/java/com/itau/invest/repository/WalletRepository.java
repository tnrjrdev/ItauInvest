package com.itau.invest.repository;

import com.itau.invest.domain.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserId(UUID userId);

    /**
     * Carrega a carteira aplicando lock otimista forcado, garantindo que a
     * versao seja verificada mesmo quando nao ha modificacao direta de campos
     * versionados antes do flush em operacoes concorrentes.
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select w from Wallet w where w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);
}
