package dev.guilhermeluan.ongoing.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserData user
) {
    public record UserData(
            Long id,
            String name,
            String email,
            boolean onboardingCompleted
    ) {
    }
}
