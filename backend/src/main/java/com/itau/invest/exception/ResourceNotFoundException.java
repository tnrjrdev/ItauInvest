package com.itau.invest.exception;

/**
 * Recurso solicitado nao encontrado (mapeada para HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s nao encontrado(a): %s".formatted(resource, id));
    }
}
