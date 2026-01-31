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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${security.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return generateToken(user);
    }

    public AuthResponse refreshToken(RefreshRequest refreshRequest) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshRequest.refreshToken())
                .orElseThrow(() -> new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        User user = token.getUser();

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByUser(user);
            throw new InvalidCredentialException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        refreshTokenRepository.delete(token);
        return generateToken(user);
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
