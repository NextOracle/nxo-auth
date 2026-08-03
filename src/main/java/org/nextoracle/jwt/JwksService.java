package org.nextoracle.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwksService {

    private final RSAPublicKey rsaPublicKey;

    @Value("${nxo-auth.jwt.issuer}")
    private String issuer;

    public Map<String, Object> getOpenIdConfiguration() {
        return Map.of(
                "issuer", issuer,
                "jwks_uri", issuer + "/oauth2/jwks",
                "response_types_supported", new String[]{"code"},
                "subject_types_supported", new String[]{"public"},
                "id_token_signing_alg_values_supported", new String[]{"RS256"},
                "token_endpoint_auth_methods_supported", new String[]{"client_secret_basic"}
        );
    }

    public Map<String, Object> getJwks() {
        JWK jwk = new RSAKey.Builder(rsaPublicKey)
                .keyID("trust-crop-key-1")
                .build();
        return new JWKSet(jwk).toJSONObject();
    }
}

