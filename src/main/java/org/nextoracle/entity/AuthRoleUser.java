package org.nextoracle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A AuthRoleUser.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "auth_role_user")
public class AuthRoleUser {

    /**
     * Unique identifier for the user-role association.
     */
    @Id
    @NotNull
    @EqualsAndHashCode.Include
    @UuidGenerator
    @Column(name = "aru_id")
    private UUID aruId;

    /**
     * Timestamp when the role was assigned to the user.
     */
    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime aruAssignedAt;

    /**
     * The user associated with the role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "aru_au_id")
    private AuthUser authUser;

    /**
     * The role associated with the user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "aru_ar_id")
    private AuthRole authRole;
}
