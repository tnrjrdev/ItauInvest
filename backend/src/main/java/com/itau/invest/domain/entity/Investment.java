package com.itau.invest.domain.entity;

import com.itau.invest.domain.enums.InvestmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Posicao de investimento de um cliente em um produto.
 *
 * <p>Mantem o principal aportado ({@code investedAmount}). A valorizacao bruta
 * atual e calculada dinamicamente na camada de servico a partir de
 * {@code appliedAt} e da taxa do produto, evitando inconsistencia entre o valor
 * persistido e o tempo decorrido.</p>
 */
@Entity
@Table(
        name = "tb_investment",
        indexes = {
                @Index(name = "ix_investment_user", columnList = "user_id"),
                @Index(name = "ix_investment_product", columnList = "product_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Total de principal aportado nesta posicao (somatorio de aplicacoes). */
    @Column(name = "invested_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal investedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    /** Instante da primeira aplicacao (base para o calculo de valorizacao). */
    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
