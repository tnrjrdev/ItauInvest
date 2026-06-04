package com.itau.invest.domain.enums;

/**
 * Tipos de movimentacao registrados no ledger (extrato) da carteira.
 */
public enum TransactionType {
    /** Entrada de recursos na carteira de caixa. */
    DEPOSIT,
    /** Saida de recursos da carteira de caixa. */
    WITHDRAWAL,
    /** Aplicacao de caixa em um produto de investimento. */
    APPLICATION,
    /** Resgate de um investimento de volta para o caixa. */
    REDEMPTION
}
