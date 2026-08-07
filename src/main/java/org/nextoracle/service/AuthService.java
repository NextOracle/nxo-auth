package org.nextoracle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.dto.AuthUserDto;
import org.nextoracle.dto.LoginRequestDto;
import org.nextoracle.dto.LoginResponseDto;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.jwt.JwtService;
import org.nextoracle.repository.AuthUserRepository;
import org.nextoracle.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthUserService authUserService;
    private final JwtService jwtService;

    public AuthUser getCurrentUser() {
        String username = SecurityUtil.getActiveUsername();
        return authUserRepository.findByAuUsername(username)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                    pd.setDetail("User not found.");
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED, pd, null);
                });
    }

    public LoginResponseDto login(LoginRequestDto request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        AuthUser user = authUserService.getByUsername(request.getUsername()).orElseThrow(() -> {
            ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
            pd.setDetail("User not found.");
            return new ErrorResponseException(HttpStatus.UNAUTHORIZED, pd, null);
        });

        List<String> roles = auth.getAuthorities().stream()
                .map(a -> Objects.requireNonNull(a.getAuthority()).replace("ROLE_", ""))
                .toList();


        // Update last login timestamp
        authUserService.updateLastLogin(user.getAuId());

        return new LoginResponseDto(
                jwtService.generate(request.getUsername(), user.getAuId(), roles, null),
                jwtService.getExpiration()
        );
    }

}