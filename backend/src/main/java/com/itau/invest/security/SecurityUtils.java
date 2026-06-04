package com.itau.invest.security;

import com.itau.invest.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utilitarios para acesso ao usuario autenticado no contexto de seguranca.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Retorna o id do usuario autenticado ou lanca erro se nao houver contexto. */
    public static UUID currentUserId() {
        return currentUser().getUserId();
    }

    public static AppUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            throw new BusinessException("Nenhum usuario autenticado no contexto");
        }
        return details;
    }
}
