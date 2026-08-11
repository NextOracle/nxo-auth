package org.nextoracle.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.aspect.IsAdmin;
import org.nextoracle.constant.Constant;
import org.nextoracle.dto.LoginRequestDto;
import org.nextoracle.dto.LoginResponseDto;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.jwt.JwtProperties;
import org.nextoracle.jwt.JwtService;
import org.nextoracle.jwt.RefreshTokenService;
import org.nextoracle.service.AuthRoleUserService;
import org.nextoracle.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

import static org.nextoracle.constant.Constant.REFRESH_TOKEN_COOKIE;

@RestController
@RequestMapping("/api/management/auth")
@Log4j2
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthRoleUserService authRoleUserService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        log.debug("Generating JWT for login request {}", loginRequestDto);

        LoginResponseDto tokenResponse = authService.login(loginRequestDto);

        // Generate refresh token and associate it with the user
        String refreshToken = refreshTokenService.generateRefreshToken(loginRequestDto.getUsername());

        // Set refresh token as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(jwtProperties.getCookie().isHttpOnly())
                .secure(jwtProperties.getCookie().isSecure())
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.getCookie().getMaxAge()))
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(tokenResponse);
    }


    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username;
        try {
            username = refreshTokenService.extractUsername(refreshToken);
        } catch (Exception _) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthUser user = authService.getCurrentUser();

        List<String> roles = authRoleUserService.findAllByUserId(user.getAuId())
                .stream()
                .map(r -> r.getAuthRole().getArName())
                .toList();

        return ResponseEntity.ok(jwtService.generate(username, user.getAuId(), roles, user.getAuProvider()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUser> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(jwtProperties.getCookie().isHttpOnly())
                .secure(jwtProperties.getCookie().isSecure())
                .path(Constant.SLASH)
                .maxAge(0)
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("Logged out successfully");
    }
}

