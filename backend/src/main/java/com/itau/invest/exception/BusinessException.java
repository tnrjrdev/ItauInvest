package com.itau.invest.exception;

/**
 * Excecao para violacoes de regra de negocio (mapeada para HTTP 422).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
