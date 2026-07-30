package org.nextoracle.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.AppRole;
import org.nextoracle.constant.Constant;
import org.nextoracle.entity.AuthRole;
import org.nextoracle.entity.AuthRoleUser;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.config.AppProperties;
import org.nextoracle.jwt.JwtProperties;
import org.nextoracle.jwt.JwtService;
import org.nextoracle.jwt.RefreshTokenService;
import org.nextoracle.service.AuthRoleService;
import org.nextoracle.service.AuthRoleUserService;
import org.nextoracle.service.AuthUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.nextoracle.constant.Constant.REFRESH_TOKEN_COOKIE;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthUserService authUserService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthRoleService authRoleService;
    private final AuthRoleUserService authRoleUserService;
    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;

    private static final String PROVIDER = "google";

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull Authentication authentication) throws IOException {

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            problemDetail.setTitle("Invalid Principal");
            problemDetail.setDetail("Principal is not an OidcUser");
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, problemDetail, null);
        }

        String email = Optional.ofNullable(oidcUser.getEmail())
                .orElseThrow(() -> {
                    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
                    problemDetail.setTitle("Missing Email");
                    problemDetail.setDetail("Email not provided by OAuth2 provider");
                    return new ErrorResponseException(HttpStatus.BAD_REQUEST, problemDetail, null);
                });
        String googleId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();

        log.info("Google OAuth2 login success for: {}", email);

        // Find or create user
        AuthUser user = getOrCreateUser(email, googleId, name, picture);

        // Update last login
        authUserService.updateLastLogin(user.getAuId());

        // Fetch roles
        List<String> roles = authRoleUserService.findAllByUserId(user.getAuId())
                .stream()
                .map(r -> r.getAuthRole().getArName())
                .toList();

        // Generate JWT
        String jwt = jwtService.generate(user.getAuUsername(), user.getAuId(), roles, user.getAuProvider());

        // Generate refresh token
        String refreshToken = refreshTokenService.generateRefreshToken(user.getAuUsername());

        // Set refresh token cookie
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(jwtProperties.getCookie().isHttpOnly())
                .secure(jwtProperties.getCookie().isSecure())
                .path(Constant.SLASH)
                .maxAge(Duration.ofMillis(jwtProperties.getCookie().getMaxAge()))
                .sameSite(jwtProperties.getCookie().getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Redirect to frontend with token
        String redirectUrl = buildRedirectUrl(jwt);

        response.sendRedirect(redirectUrl);
    }

    private String buildRedirectUrl(String jwt) {
        return appProperties.getOauth2().getRedirectUrl() + jwt;
    }

    private AuthUser getOrCreateUser(String email, String googleId, String name, String picture) {
        Optional<AuthUser> existing = authUserService.getUserByEmail(email);

        if (existing.isPresent()) {
            AuthUser user = existing.get();
            boolean changed = false;
            // Link Google provider if not already linked
            if (user.getAuProvider() == null) {
                user.setAuProvider(PROVIDER);
                user.setAuProviderId(googleId);
                changed = true;
            }
            // Logging in via Google verifies the user's email
            if (!Boolean.TRUE.equals(user.getAuIsVerified())) {
                user.setAuIsVerified(Boolean.TRUE);
                changed = true;
            }
            // Keep the avatar URL fresh on every login (Google URLs can rotate)
            if (picture != null && !picture.equals(user.getAuAvatarUrl())) {
                user.setAuAvatarUrl(picture);
                changed = true;
            }
            if (changed) {
                authUserService.partialUpdate(user);
            }
            return user;
        }

        // Create new user
        AuthUser newUser = new AuthUser();
        newUser.setAuUsername(name != null ? name : email);
        newUser.setAuEmail(email);
        newUser.setAuIsVerified(Boolean.TRUE);
        newUser.setAuProvider(PROVIDER);
        newUser.setAuProviderId(googleId);
        newUser.setAuAvatarUrl(picture);

        AuthUser savedUser = authUserService.save(newUser);

        // Assign USER role
        AuthRole role = authRoleService.getRoleByName(AppRole.USER.name());
        authRoleUserService.assignRoleToUser(new AuthRoleUser(null, null, savedUser, role));

        log.info("Created new Google user: {} ({})", email, googleId);

        return savedUser;
    }
}

