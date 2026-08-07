package org.nextoracle.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.constant.Constant;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for auth endpoints using Bucket4j (token bucket algorithm).
 * <p>
 * Limits:
 * - Login: 5 requests per minute per IP
 * - Forgot password: 3 requests per 5 minutes per IP
 * - Sign up: 3 requests per 5 minutes per IP
 */
@Component
@Log4j2
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> signUpBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!HttpMethod.POST.matches(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        Bucket bucket = getBucket(path, clientIp);

        if (bucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        checkBucket(request, response, filterChain, bucket, clientIp, path);
    }

    private void checkBucket(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain, Bucket bucket, String clientIp, String path) throws IOException, ServletException {
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Too Many Requests\"," +
               "\"status\":429,\"detail\":\"Too many requests. Please try again later.\",\"instance\":\"" + path + "\"}");
        }
    }

    private Bucket getBucket(String path, String clientIp) {
        return switch (path) {
            case "/api/management/auth/login" -> loginBuckets.computeIfAbsent(clientIp, _ -> createLoginBucket());
            case "/api/management/auth/forgot-password" -> forgotPasswordBuckets.computeIfAbsent(clientIp, _ -> createForgotPasswordBucket());
            case "/api/management/auth/sign-up" -> signUpBuckets.computeIfAbsent(clientIp, _ -> createSignUpBucket());
            default -> null;
        };
    }

    /**
     * Login: 5 attempts per minute
     */
    private Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build())
                .build();
    }

    /**
     * Forgot password: 3 attempts per 5 minutes
     */
    private Bucket createForgotPasswordBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(3).refillIntervally(3, Duration.ofMinutes(5)).build())
                .build();
    }

    /**
     * Sign up: 3 attempts per 5 minutes
     */
    private Bucket createSignUpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(3).refillIntervally(3, Duration.ofMinutes(5)).build())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(Constant.X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(Constant.COMMA)[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/management/auth/");
    }
}

