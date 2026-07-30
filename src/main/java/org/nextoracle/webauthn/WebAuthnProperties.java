package org.nextoracle.webauthn;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for the WebAuthn / Passkey Relying Party.
 *
 * <ul>
 *     <li><b>rpId</b> – the Relying Party ID, must be the site's registrable domain
 *     (e.g. {@code app.trustcrop.gr}). Passkeys are scoped to this domain.</li>
 *     <li><b>rpName</b> – a human-friendly name shown by the authenticator.</li>
 *     <li><b>allowedOrigins</b> – the full origins allowed to perform ceremonies
 *     (e.g. {@code https://app.trustcrop.gr}).</li>
 *     <li><b>challengeTtlSeconds</b> – how long a registration/login challenge stays valid.</li>
 * </ul>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nxo-auth.webauthn")
public class WebAuthnProperties {

    private String rpId;
    private String rpName;
    private List<String> allowedOrigins;
    private long challengeTtlSeconds = 300;
}

