package org.nextoracle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Login request")
public class LoginRequestDto {

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Username is required")
    private String username;

    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Password is required")
    private String password;
}
