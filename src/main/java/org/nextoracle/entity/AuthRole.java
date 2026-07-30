package org.nextoracle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * An AuthRole.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Table(name = "auth_role")
public class AuthRole {

    /**
     * Unique identifier for the role.
     */
    @Id
    @NotNull
    @EqualsAndHashCode.Include
    @UuidGenerator
    @Column(name = "ar_id")
    private UUID arId;

    /**
     * Role name (e.g. ADMIN, USER).
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "ar_name", length = 50, nullable = false, unique = true)
    private String arName;

    /**
     * Description of the role
     */
    @Column(name = "ar_description", length = 2000)
    private String arDescription;
}
