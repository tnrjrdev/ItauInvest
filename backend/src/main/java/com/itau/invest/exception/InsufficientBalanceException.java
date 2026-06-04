package com.itau.invest.exception;

/**
 * Saldo insuficiente para concluir uma operacao financeira
 * (mapeada para HTTP 422).
 */
public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
