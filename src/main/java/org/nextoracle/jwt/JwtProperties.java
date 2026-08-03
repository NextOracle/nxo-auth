package org.nextoracle.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nxo-auth.jwt")
public class JwtProperties {

    private String issuer;
    private RsaProperties rsa;
    private AccessTokenProperties accessToken;
    private RefreshTokenProperties refreshToken;
    private CookieProperties cookie;

    @Getter
    @Setter
    public static class RsaProperties {
        private String privateKeyLocation;
        private String publicKeyLocation;
    }

    @Getter
    @Setter
    public static class AccessTokenProperties {
        private String secret;
        private long expiration;
        private String username;
        private String password;
        private String role;
    }

    @Getter
    @Setter
    public static class RefreshTokenProperties {
        private String secret;
        private long expiration;
    }

    @Getter
    @Setter
    public static class CookieProperties {
        private boolean secure;
        private boolean httpOnly;
        private String sameSite;
        private long maxAge;
    }
}
