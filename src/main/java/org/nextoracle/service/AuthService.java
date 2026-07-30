package org.nextoracle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.repository.AuthUserRepository;
import org.nextoracle.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final AuthUserRepository authUserRepository;

    public AuthUser getCurrentUser() {
        String username = SecurityUtil.getActiveUsername();
        return authUserRepository.findByAuUsername(username)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
                    pd.setDetail("User not found.");
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED, pd, null);
                });
    }

}