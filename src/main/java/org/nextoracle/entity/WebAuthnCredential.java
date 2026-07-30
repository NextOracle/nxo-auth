package org.nextoracle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A WebAuthn / Passkey (FIDO2) credential belonging to an {@link AuthUser}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "publicKeyCose")
@Table(name = "webauthn_credential")
public class WebAuthnCredential {

    @Id
    @EqualsAndHashCode.Include
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Size(max = 512)
    @Column(name = "credential_id", length = 512, nullable = false, unique = true)
    private String credentialId;

    @NotNull
    @Size(max = 255)
    @Column(name = "user_handle", length = 255, nullable = false)
    private String userHandle;

    @JsonIgnore
    @NotNull
    @Column(name = "public_key_cose", nullable = false)
    private byte[] publicKeyCose;

    @Column(name = "signature_count", nullable = false)
    private long signatureCount;

    @Size(max = 255)
    @Column(name = "transports", length = 255)
    private String transports;

    @Column(name = "backup_eligible", nullable = false)
    private boolean backupEligible;

    @Column(name = "backup_state", nullable = false)
    private boolean backupState;

    @Size(max = 255)
    @Column(name = "label", length = 255)
    private String label;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}

