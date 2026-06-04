package com.itau.invest.service;

import com.itau.invest.domain.entity.User;
import com.itau.invest.domain.enums.Role;
import com.itau.invest.dto.auth.AuthResponse;
import com.itau.invest.dto.auth.LoginRequest;
import com.itau.invest.dto.auth.RegisterRequest;
import com.itau.invest.exception.DuplicateResourceException;
import com.itau.invest.repository.UserRepository;
import com.itau.invest.repository.WalletRepository;
import com.itau.invest.security.AppUserDetails;
import com.itau.invest.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Maria Silva", "maria@itau.com.br",
                "52998224725", "SenhaForte@123");
    }

    @Test
    void shouldRegisterNewClientAndProvisionWallet() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(userId);
            return u;
        });
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("token-123");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.accessToken()).isEqualTo("token-123");
        assertThat(response.role()).isEqualTo(Role.CLIENT.name());
        verify(walletRepository).save(any());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateCpf() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(walletRepository, never()).save(any());
    }

    @Test
    void shouldLoginAndReturnToken() {
        User user = User.builder().name("Maria").email("maria@itau.com.br")
                .cpf("52998224725").passwordHash("hash").role(Role.CLIENT).active(true).build();
        user.setId(UUID.randomUUID());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                new AppUserDetails(user), null);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(), anyString(), anyString())).thenReturn("jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(
                new LoginRequest("maria@itau.com.br", "SenhaForte@123"));

        assertThat(response.accessToken()).isEqualTo("jwt");
        assertThat(response.name()).isEqualTo("Maria");
    }
}
