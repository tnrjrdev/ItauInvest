package com.itau.invest.domain.enums;

/**
 * Perfis de acesso do sistema. Mapeados para authorities do Spring Security
 * com o prefixo {@code ROLE_}.
 */
public enum Role {
    /** Administrador: gerencia o catalogo de produtos e visualiza clientes. */
    ADMIN,
    /** Cliente: opera a propria carteira e investimentos. */
    CLIENT
}
