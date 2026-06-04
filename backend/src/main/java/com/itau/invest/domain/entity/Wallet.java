package com.itau.invest.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Carteira de caixa do cliente. Mantem o saldo disponivel para aplicacoes,
 * depositos e saques.
 *
 * <p>O campo {@link #version} habilita optimistic locking, protegendo o saldo
 * contra condicoes de corrida em operacoes financeiras concorrentes.</p>
 */
@Entity
@Table(name = "tb_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Credita um valor no saldo de caixa. */
    public void credit(BigDecimal amount) {
        this.cashBalance = this.cashBalance.add(amount);
    }

    /** Debita um valor do saldo de caixa. */
    public void debit(BigDecimal amount) {
        this.cashBalance = this.cashBalance.subtract(amount);
    }

    /** Indica se ha saldo suficiente para um debito do valor informado. */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.cashBalance.compareTo(amount) >= 0;
    }
}
