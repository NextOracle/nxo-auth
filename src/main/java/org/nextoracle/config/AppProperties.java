package org.nextoracle.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level configuration properties for the NxO Auth library.
 *
 * <p>All properties live under the {@code nxo-auth} prefix in the consuming
 * application's {@code application.yml} / {@code application.properties}.</p>
 *
 * <pre>{@code
 * nxo-auth:
 *   frontend-url: https://app.example.com
 *   whitelist:
 *     - /api/public/**
 *     - /actuator/**
 *   oauth2:
 *     redirect-url: https://app.example.com/oauth2/callback?token=
 * }</pre>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nxo-auth")
public class AppProperties {

    /**
     * Base URL of the front-end application (used e.g. for OAuth2 redirects).
     */
    private String frontendUrl;

    /**
     * URL patterns that bypass JWT authentication entirely.
     */
    private List<String> whitelist = new ArrayList<>();

    /**
     * OAuth2 / Social-login settings.
     */
    private OAuth2Properties oauth2 = new OAuth2Properties();

    @Getter
    @Setter
    public static class OAuth2Properties {
        /**
         * URL to redirect the browser to after a successful OAuth2 login.
         * The JWT access token is appended as a query-string parameter.
         * Example: {@code https://app.example.com/oauth2/callback?token=}
         */
        private String redirectUrl;
    }
}

