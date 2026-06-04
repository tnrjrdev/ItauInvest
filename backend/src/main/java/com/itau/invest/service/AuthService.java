package com.itau.invest.service;

import com.itau.invest.domain.entity.User;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.domain.enums.Role;
import com.itau.invest.dto.auth.AuthResponse;
import com.itau.invest.dto.auth.LoginRequest;
import com.itau.invest.dto.auth.RegisterRequest;
import com.itau.invest.exception.DuplicateResourceException;
import com.itau.invest.repository.UserRepository;
import com.itau.invest.repository.WalletRepository;
import com.itau.invest.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servico de autenticacao: registro de clientes e emissao de tokens JWT.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registra um novo cliente e provisiona sua carteira de caixa zerada.
     * O perfil atribuido e sempre {@link Role#CLIENT}.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ja existe um usuario com este e-mail");
        }
        if (userRepository.existsByCpf(request.cpf())) {
            throw new DuplicateResourceException("Ja existe um usuario com este CPF");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .cpf(request.cpf())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .cashBalance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        log.info("Novo cliente registrado: userId={}", user.getId());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.bearer(token, jwtService.getExpirationSeconds(),
                user.getId(), user.getName(), user.getRole().name());
    }

    /**
     * Autentica o usuario por e-mail/senha e emite um access token.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = ((com.itau.invest.security.AppUserDetails) authentication.getPrincipal())
                .getDomainUser();

        log.info("Login bem-sucedido: userId={}", user.getId());

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.bearer(token, jwtService.getExpirationSeconds(),
                user.getId(), user.getName(), user.getRole().name());
    }
}
