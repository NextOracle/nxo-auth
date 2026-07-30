package org.nextoracle.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.nextoracle.dto.AuthUserDetailsDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFactory {

    private final JwtService jwtService;

    public UsernamePasswordAuthenticationToken create(String token) {
        Claims claims = jwtService.parse(token);

        String username = claims.getSubject();
        String userIdStr = claims.get("userId", String.class);
        UUID userId = userIdStr != null ? UUID.fromString(userIdStr) : null;
        List<?> roles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();

        AuthUserDetailsDto principal = AuthUserDetailsDto.builder()
                .userId(userId)
                .username(username)
                .password(null)
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}

