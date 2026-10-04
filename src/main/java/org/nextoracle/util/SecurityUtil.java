package org.nextoracle.util;

import lombok.experimental.UtilityClass;
import org.nextoracle.AuthUserDetails;
import org.nextoracle.dto.AuthUserDetailsDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility class for security-related operations, such as extracting user information from the JWT token.
 * This class is not meant to be instantiated, hence the private constructor.
 */
@UtilityClass
public class SecurityUtil {

    /**
     * Get the active username from the security context.
     */
    public static String getActiveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Objects.requireNonNull(Objects.requireNonNull(authentication).getPrincipal());
        return principal instanceof UserDetails userDetails
                ? userDetails.getUsername()
                : principal.toString();
    }

    /**
     * Get the active user UUID from the security context.
     * The principal is {@link AuthUserDetailsDto} when authenticated through a JWT and
     * {@link AuthUserDetails} when authenticated through the {@code UserDetailsService}.
     */
    public static UUID getActiveUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Objects.requireNonNull(Objects.requireNonNull(authentication).getPrincipal());
        if (principal instanceof AuthUserDetailsDto dto) {
            return dto.getUserId();
        }
        if (principal instanceof AuthUserDetails details) {
            return details.getUserId();
        }
        return null;
    }

    public static List<String> getActiveRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Objects.requireNonNull(authentication).getAuthorities().stream()
                .map(a -> Objects.requireNonNull(a.getAuthority()).replace("ROLE_", ""))
                .toList();
    }
}
