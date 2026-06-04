package com.itau.invest.domain.entity;

import com.itau.invest.domain.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lancamento imutavel no ledger (extrato) da carteira.
 *
 * <p>Cada operacao financeira gera um registro append-only contendo o valor e o
 * saldo de caixa resultante ({@code balanceAfter}), garantindo auditabilidade.</p>
 */
@Entity
@Table(
        name = "tb_transaction",
        indexes = {
                @Index(name = "ix_transaction_wallet", columnList = "wallet_id"),
                @Index(name = "ix_transaction_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /** Investimento relacionado (apenas para APPLICATION/REDEMPTION). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id")
    private Investment investment;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 15)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** Saldo de caixa da carteira apos a aplicacao deste lancamento. */
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 255)
    private String description;
}
