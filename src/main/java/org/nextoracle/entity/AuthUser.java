package org.nextoracle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An AuthUser.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "auth_user")
public class AuthUser {

    /**
     * Unique identifier for the user.
     */
    @Id
    @EqualsAndHashCode.Include
    @UuidGenerator
    @Column(name = "au_id", updatable = false, nullable = false)
    private UUID auId;

    /**
     * Username used for login (must be unique).
     */
    @NotNull
    @Size(max = 100)
    @Column(name = "au_username", length = 100, nullable = false)
    private String auUsername;

    /**
     * Timestamp when the user was created.
     */
    @CreationTimestamp
    @Column(name = "au_created_at", nullable = false)
    private LocalDateTime auCreatedAt;

    /**
     * User's email address.
     */
    @Email
    @Size(max = 255)
    @Column(name = "au_email")
    private String auEmail;

    /**
     * Indicates whether the user's email has been verified.
     */
    @Column(name = "au_is_verified", nullable = false)
    private Boolean auIsVerified = false;

    /**
     * Timestamp of the user's last login.
     */
    @UpdateTimestamp
    @Column(name = "au_last_login")
    private LocalDateTime auLastLogin;

    /**
     * OAuth2 provider name (e.g., "google", "apple").
     */
    @Size(max = 50)
    @Column(name = "au_provider", length = 50)
    private String auProvider;

    /**
     * The user's unique ID from the OAuth2 provider.
     */
    @Size(max = 255)
    @Column(name = "au_provider_id")
    private String auProviderId;

    /**
     * URL of the user's avatar/profile picture (e.g. provided by Google OAuth2).
     * Stored as a plain URL string — the image itself is served by the provider's CDN.
     */
    @Size(max = 512)
    @Column(name = "au_avatar_url", length = 512)
    private String auAvatarUrl;
}
