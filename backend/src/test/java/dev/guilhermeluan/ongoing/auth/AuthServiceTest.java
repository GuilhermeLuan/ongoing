// HU02 - Realizar Logon | HU03 - Realizar Cadastro
package dev.guilhermeluan.ongoing.auth;

import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.LoginRequest;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.auth.jwt.JwtService;
import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.InvalidCredentialException;
import dev.guilhermeluan.ongoing.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 3600000L);
    }

    // HU03 - CA1: cadastro com sucesso ao fornecer dados válidos
    @Test
    void register_ShouldReturnAuthResponse_WhenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(jwtService.createToken(any(User.class))).thenReturn("accessToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertNotNull(response.refreshToken());
        verify(userRepository).save(any(User.class));
    }

    // HU03 - CA2: erro quando e-mail já está cadastrado na base
    @Test
    void register_ShouldThrowBadRequestException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already exists", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    // HU02 - CA1: login com sucesso com credenciais válidas
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("accessToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertNotNull(response.refreshToken());
    }

    // HU02 - CA2: erro quando e-mail não está cadastrado
    @Test
    void login_ShouldThrowInvalidCredentialException_WhenEmailNotFound() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialException.class, () -> authService.login(request));
    }

    // HU02 - CA2: erro quando senha não confere (credenciais incorretas)
    @Test
    void login_ShouldThrowInvalidCredentialException_WhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongPassword");
        User user = User.builder()
                .id(1L)
                .email("john@example.com")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialException.class, () -> authService.login(request));
    }
}
