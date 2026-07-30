package org.nextoracle.webauthn.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body posted to start passkey login. The {@code username} may be the user's
 * username or email; it is used to look up the allowed credentials.
 */
public record WebAuthnLoginStartRequest(@NotBlank String username) {
}

