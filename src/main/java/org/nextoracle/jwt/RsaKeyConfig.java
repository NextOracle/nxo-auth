package org.nextoracle.jwt;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Log4j2
@Configuration
public class RsaKeyConfig {

    @Value("${nxo-auth.jwt.rsa.private-key-location}")
    private Resource privateKeyResource;

    @Value("${nxo-auth.jwt.rsa.public-key-location}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        RSAPrivateKey rsaPrivateKey;
        try {
            String key = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (IOException _) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load RSA private key"),null);
        } catch (InvalidKeySpecException _) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid RSA private key format"),null);
        } catch (NoSuchAlgorithmException _) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "RSA algorithm not supported"),null);
        }
        return rsaPrivateKey;
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        RSAPublicKey rsaPublicKey;
        try {
            String key = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        } catch (IOException _) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load RSA public key"),null);
        } catch (InvalidKeySpecException _) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid RSA public key format"),null);
        } catch (NoSuchAlgorithmException e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "RSA algorithm not supported"),null);
        }
        return rsaPublicKey;
    }
}

