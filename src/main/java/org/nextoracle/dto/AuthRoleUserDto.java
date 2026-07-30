package org.nextoracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.nextoracle.entity.AuthRoleUser;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A DTO for the {@link AuthRoleUser} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Schema(description = "DTO representing the association between a user and a role.")
public class AuthRoleUserDto {

    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Unique identifier for the user-role association.")
    private UUID aruId;

    @NotNull
    @Schema(description = "Timestamp when the role was assigned to the user.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime aruAssignedAt;

    @ToString.Exclude
    @Schema(description = "The associated user.")
    private AuthUserDto authUser;

    @ToString.Exclude
    @Schema(description = "The associated role.")
    private AuthRoleDto authRole;
}

