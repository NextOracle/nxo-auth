package org.nextoracle.config;

import lombok.RequiredArgsConstructor;
import org.nextoracle.config.AppProperties;
import org.nextoracle.AppRole;
import org.nextoracle.jwt.JwtAuthenticationEntryPoint;
import org.nextoracle.jwt.JwtAuthenticationFactory;
import org.nextoracle.jwt.JwtFilter;
import org.nextoracle.oauth2.OAuth2LoginSuccessHandler;
import org.nextoracle.ratelimit.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFactory authFactory;

    private final AppProperties appProperties;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    private final RateLimitFilter rateLimitFilter;

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // CSRF disabled: all state-mutating endpoints require a JWT Bearer token in the
                // Authorization header. Browsers cannot forge that header in cross-origin requests,
                // so there is no CSRF vector. Cookie/session credentials are never used for auth.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(appProperties.getWhitelist().toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(authFactory, appProperties);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter filter) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }



    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(AppRole.ADMIN.name()).implies(AppRole.ENTERPRISE.name())
                .role(AppRole.ENTERPRISE.name()).implies(AppRole.PREMIUM.name())
                .role(AppRole.PREMIUM.name()).implies(AppRole.STANDARD.name())
                .role(AppRole.STANDARD.name()).implies(AppRole.USER.name())
                .build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }
}
