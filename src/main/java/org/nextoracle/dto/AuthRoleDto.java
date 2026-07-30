package org.nextoracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.nextoracle.entity.AuthRole;

import java.util.UUID;

/**
 * A DTO for the {@link AuthRole} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Schema(description = "DTO representing an application role.")
public class AuthRoleDto {

    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Unique identifier for the role.")
    private UUID arId;

    @NotNull
    @Size(max = 50)
    @Schema(description = "Role name (e.g. ADMIN, USER). Do NOT prefix with ROLE_.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String arName;

    @Schema(description = "Description of the role.")
    private String arDescription;
}

