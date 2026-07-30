package org.nextoracle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "User with assigned roles")
public record AuthUserWithRolesDto(

        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID userId,

        @Schema(description = "Username", example = "john_doe")
        String username,

        @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last login timestamp", example = "2026-05-29T08:00:00")
        LocalDateTime lastLogin,

        @Schema(description = "Comma-separated list of assigned roles", example = "ADMIN,USER")
        String roles,

        @Schema(description = "Email address", example = "john@example.com")
        String email
) {
}
