package org.nextoracle.webauthn;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.constant.Constant;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.entity.WebAuthnCredential;
import org.nextoracle.jwt.JwtProperties;
import org.nextoracle.jwt.JwtService;
import org.nextoracle.jwt.RefreshTokenService;
import org.nextoracle.service.AuthRoleUserService;
import org.nextoracle.service.AuthUserService;
import org.nextoracle.util.SecurityUtil;
import org.nextoracle.webauthn.dto.WebAuthnLoginStartRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.nextoracle.constant.Constant.REFRESH_TOKEN_COOKIE;

@RestController
@RequestMapping("/api/management/auth/webauthn")
@Log4j2
@RequiredArgsConstructor
public class WebAuthnController {

    private final WebAuthnService webAuthnService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthRoleUserService authRoleUserService;
    private final AuthUserService authUserService;
    private final JwtProperties jwtProperties;

    // ----------------------------- Registration -----------------------------

    @PostMapping(value = "/register/options", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerOptions() {
        String username = SecurityUtil.getActiveUsername();
        log.debug("WebAuthn registration options requested for {}", username);
        return ResponseEntity.ok(webAuthnService.startRegistration(username));
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> register(@RequestBody String body) {
        String username = SecurityUtil.getActiveUsername();
        webAuthnService.finishRegistration(username, body);
        return ResponseEntity.ok().build();
    }

    // ------------------------------- Login ----------------------------------

    @PostMapping(value = "/login/options", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loginOptions(@Valid @RequestBody WebAuthnLoginStartRequest request) {
        log.debug("WebAuthn login options requested for {}", request.username());
        return ResponseEntity.ok(webAuthnService.startLogin(request.username()));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody String body, HttpServletResponse response) {
        AuthUser user = webAuthnService.finishLogin(body);

        List<String> roles = authRoleUserService.findAllByUserId(user.getAuId())
                .stream()
                .map(r -> r.getAuthRole().getArName())
                .toList();

        authUserService.updateLastLogin(user.getAuId());

        String jwt = jwtService.generate(user.getAuUsername(), user.getAuId(), roles, user.getAuProvider());

        String refreshToken = refreshTokenService.generateRefreshToken(user.getAuUsername());
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(jwtProperties.getCookie().isHttpOnly())
                .secure(jwtProperties.getCookie().isSecure())
                .path(Constant.SLASH)
                .maxAge(Duration.ofMillis(jwtProperties.getCookie().getMaxAge()))
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(jwt);
    }

    // ------------------------ Credential management -------------------------

    @GetMapping("/credentials")
    public ResponseEntity<List<WebAuthnCredential>> listCredentials() {
        UUID userId = SecurityUtil.getActiveUUID();
        return ResponseEntity.ok(webAuthnService.listCredentials(userId));
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getActiveUUID();
        webAuthnService.deleteCredential(userId, id);
        return ResponseEntity.noContent().build();
    }
}
