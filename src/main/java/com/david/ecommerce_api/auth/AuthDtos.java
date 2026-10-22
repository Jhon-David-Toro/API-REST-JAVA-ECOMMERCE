package com.david.ecommerce_api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 6, max = 72) String password
    ) {
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record ProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email
    ) {
    }

    public record PasswordRequest(
            @NotBlank String current,
            @NotBlank @Size(min = 6, max = 72) String next
    ) {
    }

    public record BlockRequest(boolean blocked) {
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            String role,
            boolean blocked,
            long createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().apiValue(),
                    user.isBlocked(),
                    user.getCreatedAt().toEpochMilli()
            );
        }
    }

    public record AuthResponse(
            String token,
            UserResponse user
    ) {
    }
}
