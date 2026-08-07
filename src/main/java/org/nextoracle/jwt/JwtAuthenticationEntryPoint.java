package org.nextoracle.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Sends a 401 Unauthorized response when authentication fails.
 * <p>
 * The {@link ProblemDetail} body is written <strong>directly</strong> to the response instead of
 * calling {@link HttpServletResponse#sendError}. {@code sendError} triggers an internal servlet
 * ERROR dispatch to {@code /error}, which re-enters the security filter chain ({@code JwtFilter} →
 * this entry point), producing a second, misleading "Unauthorized access attempt to /error" warning.
 * Writing the response directly terminates the request here and avoids that spurious re-dispatch.
 */
@Component
@Log4j2
public final class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException
    ) throws IOException {

        // Log the unauthorized access attempt with minimal performance impact
        log.warn("Unauthorized access attempt to {}: {}", request.getRequestURI(), authException.getMessage());

        if (response.isCommitted()) {
            // Nothing we can do (e.g. a WebSocket/SockJS transport already started writing)
            return;
        }

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized access");
        problem.setDetail("Valid authentication is required to access this resource.");
        problem.setInstance(URI.create(request.getRequestURI()));

        // Write the 401 body directly — no sendError(), so no internal forward to /error
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), problem);
    }
}
