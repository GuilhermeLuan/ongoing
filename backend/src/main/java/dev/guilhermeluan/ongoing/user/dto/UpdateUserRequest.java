package dev.guilhermeluan.ongoing.user.dto;

public record UpdateUserRequest(
        String name,
        Boolean onboardingCompleted
) {
}
