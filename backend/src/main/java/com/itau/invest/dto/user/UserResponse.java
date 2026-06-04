package com.itau.invest.dto.user;

import com.itau.invest.domain.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String role,
        boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getRole().name(),
                user.isActive()
        );
    }
}
