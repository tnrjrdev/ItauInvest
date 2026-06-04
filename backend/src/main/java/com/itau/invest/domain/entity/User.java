package com.itau.invest.domain.entity;

import com.itau.invest.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario do sistema (credencial + perfil). Cada cliente possui exatamente
 * uma {@link Wallet}.
 */
@Entity
@Table(
        name = "tb_user",
        indexes = {
                @Index(name = "ux_user_email", columnList = "email", unique = true),
                @Index(name = "ux_user_cpf", columnList = "cpf", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    /** Hash BCrypt da senha. Nunca armazenar senha em texto puro. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToOne(mappedBy = "user")
    private Wallet wallet;
}
