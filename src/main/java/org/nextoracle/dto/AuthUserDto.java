package org.nextoracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.nextoracle.entity.AuthUser;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A DTO for the {@link AuthUser} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Schema(description = "DTO representing an application user.")
public class AuthUserDto {

    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Unique identifier for the user.")
    private UUID auId;

    @NotNull
    @Size(max = 100)
    @Schema(description = "Username used for login (must be unique).",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String auUsername;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Timestamp when the user was created.")
    private LocalDateTime auCreatedAt;

    @Email
    @Size(max = 255)
    @Schema(description = "Email address of the user.")
    private String auEmail;

    @Schema(description = "Whether the user's email has been verified.")
    private Boolean auIsVerified;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Timestamp of the user's last login.")
    private LocalDateTime auLastLogin;

    @NotNull
    @Size(max = 50)
    @Schema(description = "OAuth2 provider name (e.g. google, apple). Null for local users.")
    private String auProvider;

    @NotNull
    @Size(max = 255)
    @Schema(description = "User's unique ID from the OAuth2 provider.")
    private String auProviderId;

    @Size(max = 512)
    @Schema(description = "URL of the user's avatar/profile picture.")
    private String auAvatarUrl;
}

