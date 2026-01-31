package dev.guilhermeluan.ongoing.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
