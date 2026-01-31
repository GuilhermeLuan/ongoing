package dev.guilhermeluan.ongoing.auth;

import dev.guilhermeluan.ongoing.auth.dto.AuthResponse;
import dev.guilhermeluan.ongoing.auth.dto.LoginRequest;
import dev.guilhermeluan.ongoing.auth.dto.RefreshRequest;
import dev.guilhermeluan.ongoing.auth.dto.RegisterRequest;
import dev.guilhermeluan.ongoing.auth.jwt.JwtService;
import dev.guilhermeluan.ongoing.exception.BadRequestException;
import dev.guilhermeluan.ongoing.exception.InvalidCredentialException;
import dev.guilhermeluan.ongoing.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    @Value("${security.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return generateToken(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return generateToken(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshRequest refreshRequest) {
        // Busca o token (lança exceção se não encontrado)
        RefreshToken token = refreshTokenService.findByTokenOrThrowInvalidCredentialException(refreshRequest.refreshToken());

        // Se expirado: deletar em transação separada e lançar exceção
        if (refreshTokenService.isExpired(token)) {
            refreshTokenService.deleteTokenInNewTransaction(token.getId());
            throw new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // Token válido: deletar na mesma transação (rollback se falhar depois)
        refreshTokenRepository.delete(token);
        return generateToken(token.getUser());
    }

    private AuthResponse generateToken(User user) {
        String accessToken = jwtService.createToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }
}
