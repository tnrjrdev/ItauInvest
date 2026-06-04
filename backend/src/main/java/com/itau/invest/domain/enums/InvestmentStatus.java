package com.itau.invest.domain.enums;

/**
 * Estado de uma posicao de investimento do cliente.
 */
public enum InvestmentStatus {
    /** Posicao ativa, sujeita a valorizacao. */
    ACTIVE,
    /** Posicao totalmente resgatada. */
    REDEEMED
}
