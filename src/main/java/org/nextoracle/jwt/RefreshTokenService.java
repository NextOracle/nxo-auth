package org.nextoracle.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getRefreshToken().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT refresh token for a given username
     *
     * @param username The username to embed in the refresh token
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String username) {
        log.debug("Generate refresh token for user: {}", username);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshToken().getExpiration());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the username from a refresh token
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}