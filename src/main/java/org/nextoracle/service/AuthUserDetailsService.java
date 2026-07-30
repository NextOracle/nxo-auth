package org.nextoracle.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.nextoracle.AuthUserDetails;
import org.nextoracle.dto.AuthRoleUserDto;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.repository.AuthUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final AuthRoleUserService authRoleUserService;

    @Override
    public @NotNull UserDetails loadUserByUsername(@NotNull String username) {
        AuthUser user = authUserRepository.findByAuUsername(username)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                    pd.setDetail("User not found: " + username);
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED, pd, null);
                });

        Set<GrantedAuthority> authorities = authRoleUserService.findAllByUserId(user.getAuId())
                .stream()
                .map(AuthRoleUserDto::getAuthRole)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getArName()))
                .collect(Collectors.toUnmodifiableSet());

        return AuthUserDetails.builder()
                .userId(user.getAuId())
                .username(user.getAuUsername())
                .password(null)
                .authorities(authorities)
                .build();
    }
}
