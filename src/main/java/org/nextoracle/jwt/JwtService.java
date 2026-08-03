package org.nextoracle.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtService {

    private final JwtProperties jwtProperties;
    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;

    @Value("${nxo-auth.jwt.issuer}")
    private String issuer;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getAccessToken().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // HMAC-based token generation with userId claim
    public String generate(String username, UUID userId, List<String> roles) {
        return generate(username, userId, roles, null);
    }

    // HMAC-based token generation with userId and provider claims
    public String generate(String username, UUID userId, List<String> roles, String provider) {
        log.debug("Generate Access token (HMAC) for user: {}", username);
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .claim("provider", provider != null ? provider : "local")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessToken().getExpiration()))
                .signWith(signingKey())
                .compact();
    }

    // RSA-based token generation for inter-service communication with userId claim
    public String generateRsa(String username, UUID userId, List<String> roles) {
        log.debug("Generate Access token (RSA) for user: {}", username);
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtProperties.getAccessToken().getExpiration()))
                .signWith(rsaPrivateKey)
                .compact();
    }

    // Existing HMAC-based parse (unchanged)
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // NEW: RSA-based parse
    public Claims parseRsa(String token) {
        return Jwts.parser()
                .verifyWith(rsaPublicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiration() {
        return jwtProperties.getAccessToken().getExpiration();
    }
}
