package dev.guilhermeluan.ongoing.user.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        boolean onboardingCompleted
) {
}
