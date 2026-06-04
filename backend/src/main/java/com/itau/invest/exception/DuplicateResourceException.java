package com.itau.invest.exception;

/**
 * Tentativa de criar um recurso que viola uma restricao de unicidade
 * (mapeada para HTTP 409).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
