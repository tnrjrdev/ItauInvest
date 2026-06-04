package com.itau.invest.config;

import com.itau.invest.domain.entity.User;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.domain.enums.Role;
import com.itau.invest.repository.UserRepository;
import com.itau.invest.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Provisiona credenciais iniciais (ADMIN e cliente demo) na primeira execucao.
 * As senhas sao geradas com BCrypt em runtime. Desabilitado no perfil de teste.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           WalletRepository walletRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("admin@itau.com.br", "00000000000", "Administrador",
                Role.ADMIN, "Admin@123", BigDecimal.ZERO);
        seedUser("cliente@itau.com.br", "52998224725", "Cliente Demo",
                Role.CLIENT, "Cliente@123", new BigDecimal("10000.0000"));
    }

    private void seedUser(String email, String cpf, String name, Role role,
                          String rawPassword, BigDecimal initialBalance) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = userRepository.save(User.builder()
                .name(name)
                .email(email)
                .cpf(cpf)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(true)
                .build());

        walletRepository.save(Wallet.builder()
                .user(user)
                .cashBalance(initialBalance)
                .build());

        log.info("Usuario seed criado: {} ({})", email, role);
    }
}
