package org.nextoracle.webauthn;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Builds the Yubico {@link RelyingParty} used to start/finish WebAuthn ceremonies.
 */
@Configuration
@RequiredArgsConstructor
public class WebAuthnConfig {

    private final WebAuthnProperties properties;
    private final JpaWebAuthnCredentialRepository credentialRepository;

    @Bean
    public RelyingParty relyingParty() {
        RelyingPartyIdentity identity = RelyingPartyIdentity.builder()
                .id(properties.getRpId())
                .name(properties.getRpName())
                .build();

        return RelyingParty.builder()
                .identity(identity)
                .credentialRepository(credentialRepository)
                .origins(Set.copyOf(properties.getAllowedOrigins()))
                .allowOriginPort(true)
                .build();
    }
}

