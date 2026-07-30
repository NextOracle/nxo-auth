package org.nextoracle.webauthn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.entity.WebAuthnCredential;
import org.nextoracle.repository.AuthUserRepository;
import org.nextoracle.repository.WebAuthnCredentialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class WebAuthnService {

    private final RelyingParty relyingParty;
    private final WebAuthnRequestRegistry requestRegistry;
    private final WebAuthnCredentialRepository credentialRepository;
    private final AuthUserRepository authUserRepository;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    // ---------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------

    public String startRegistration(String username) {
        AuthUser user = requireUser(username);

        UserIdentity userIdentity = UserIdentity.builder()
                .name(user.getAuUsername())
                .displayName(user.getAuEmail() != null ? user.getAuEmail() : user.getAuUsername())
                .id(WebAuthnIds.toUserHandle(user.getAuId()))
                .build();

        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(userIdentity)
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                .residentKey(ResidentKeyRequirement.PREFERRED)
                                .userVerification(UserVerificationRequirement.PREFERRED)
                                .build())
                        .build());

        String requestId = requestRegistry.store(options);
        return buildOptionsResponse(requestId, credentialsJson(options::toCredentialsCreateJson));
    }

    @Transactional
    @SuppressWarnings("deprecation")
    public void finishRegistration(String username, String rawBody) {
        AuthUser user = requireUser(username);

        JsonNode body = readTree(rawBody);
        String requestId = requiredText(body, "requestId");
        String label = body.hasNonNull("label") ? body.get("label").asText() : null;
        JsonNode credentialNode = requiredNode(body, "credential");

        PublicKeyCredentialCreationOptions options = requestRegistry
                .consume(requestId, PublicKeyCredentialCreationOptions.class)
                .orElseThrow(() -> badRequest("Expired Request", "Registration challenge expired. Please try again."));

        try {
            var credential = com.yubico.webauthn.data.PublicKeyCredential
                    .parseRegistrationResponseJson(jsonMapper.writeValueAsString(credentialNode));

            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(options)
                    .response(credential)
                    .build());

            String credentialId = result.getKeyId().getId().getBase64Url();
            if (credentialRepository.existsByCredentialId(credentialId)) {
                throw badRequest("Already Registered", "This passkey is already registered.");
            }

            String transports = result.getKeyId().getTransports()
                    .map(set -> set.stream().map(AuthenticatorTransport::getId).collect(Collectors.joining(",")))
                    .orElse(null);

            var flags = credential.getResponse().getParsedAuthenticatorData().getFlags();

            WebAuthnCredential entity = WebAuthnCredential.builder()
                    .userId(user.getAuId())
                    .credentialId(credentialId)
                    .userHandle(options.getUser().getId().getBase64Url())
                    .publicKeyCose(result.getPublicKeyCose().getBytes())
                    .signatureCount(result.getSignatureCount())
                    .transports(transports)
                    .backupEligible(flags.BE)
                    .backupState(flags.BS)
                    .label(label)
                    .build();

            credentialRepository.save(entity);
            log.info("Registered new passkey for user {}", user.getAuUsername());

        } catch (RegistrationFailedException e) {
            log.warn("Passkey registration failed for {}: {}", username, e.getMessage());
            throw badRequest("Registration Failed", "Passkey registration could not be verified.");
        } catch (IOException e) {
            log.warn("Malformed passkey registration payload for {}: {}", username, e.getMessage());
            throw badRequest("Invalid Payload", "The registration response was malformed.");
        }
    }

    // ---------------------------------------------------------------------
    // Authentication
    // ---------------------------------------------------------------------

    public String startLogin(String usernameOrEmail) {
        AuthUser user = resolveUser(usernameOrEmail)
                .orElseThrow(() -> unauthorized("No passkey is registered for this account."));

        AssertionRequest assertionRequest = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(user.getAuUsername())
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build());

        String requestId = requestRegistry.store(assertionRequest);
        return buildOptionsResponse(requestId, credentialsJson(assertionRequest::toCredentialsGetJson));
    }

    @Transactional
    public AuthUser finishLogin(String rawBody) {
        JsonNode body = readTree(rawBody);
        String requestId = requiredText(body, "requestId");
        JsonNode credentialNode = requiredNode(body, "credential");

        AssertionRequest assertionRequest = requestRegistry
                .consume(requestId, AssertionRequest.class)
                .orElseThrow(() -> badRequest("Expired Request", "Login challenge expired. Please try again."));

        try {
            var credential = com.yubico.webauthn.data.PublicKeyCredential
                    .parseAssertionResponseJson(jsonMapper.writeValueAsString(credentialNode));

            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertionRequest)
                    .response(credential)
                    .build());

            if (!result.isSuccess()) {
                throw unauthorized("Passkey authentication failed.");
            }

            credentialRepository.findByCredentialId(result.getCredential().getCredentialId().getBase64Url())
                    .ifPresent(stored -> {
                        stored.setSignatureCount(result.getSignatureCount());
                        stored.setLastUsedAt(LocalDateTime.now(java.time.ZoneId.of("Europe/Athens")));
                        credentialRepository.save(stored);
                    });

            return authUserRepository.findByAuUsername(result.getUsername())
                    .orElseThrow(() -> unauthorized("User not found for passkey."));

        } catch (AssertionFailedException e) {
            log.warn("Passkey assertion failed: {}", e.getMessage());
            throw unauthorized("Passkey authentication failed.");
        } catch (IOException e) {
            log.warn("Malformed passkey assertion payload: {}", e.getMessage());
            throw badRequest("Invalid Payload", "The login response was malformed.");
        }
    }

    // ---------------------------------------------------------------------
    // Credential management
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<WebAuthnCredential> listCredentials(UUID userId) {
        return credentialRepository.findAllByUserId(userId);
    }

    @Transactional
    public void deleteCredential(UUID userId, UUID credentialId) {
        WebAuthnCredential credential = credentialRepository.findByIdAndUserId(credentialId, userId)
                .orElseThrow(() -> badRequest("Not Found", "Passkey not found."));
        credentialRepository.delete(credential);
        log.info("Deleted passkey {} for user {}", credentialId, userId);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private String buildOptionsResponse(String requestId, String credentialsJson) {
        try {
            ObjectNode root = jsonMapper.createObjectNode();
            root.put("requestId", requestId);
            root.set("publicKey", jsonMapper.readTree(credentialsJson).get("publicKey"));
            return jsonMapper.writeValueAsString(root);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to assemble WebAuthn options response", e);
        }
    }

    private String credentialsJson(CredentialsJsonSupplier supplier) {
        try {
            return supplier.get();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize WebAuthn options", e);
        }
    }

    @FunctionalInterface
    private interface CredentialsJsonSupplier {
        String get() throws com.fasterxml.jackson.core.JsonProcessingException;
    }

    private JsonNode readTree(String rawBody) {
        try {
            return jsonMapper.readTree(rawBody);
        } catch (IOException e) {
            throw badRequest("Invalid Payload", "Request body is not valid JSON.");
        }
    }

    private String requiredText(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw badRequest("Invalid Payload", "Missing required field: " + field);
        }
        return node.asText();
    }

    private JsonNode requiredNode(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            throw badRequest("Invalid Payload", "Missing required field: " + field);
        }
        return node;
    }

    private AuthUser requireUser(String username) {
        return authUserRepository.findByAuUsername(username)
                .orElseThrow(() -> unauthorized("User not found."));
    }

    private Optional<AuthUser> resolveUser(String usernameOrEmail) {
        return authUserRepository.findByAuUsername(usernameOrEmail)
                .or(() -> authUserRepository.findByAuEmail(usernameOrEmail));
    }

    private ErrorResponseException badRequest(String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle(title);
        problem.setDetail(detail);
        return new ErrorResponseException(HttpStatus.BAD_REQUEST, problem, null);
    }

    private ErrorResponseException unauthorized(String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setTitle("Unauthorized");
        problem.setDetail(detail);
        return new ErrorResponseException(HttpStatus.UNAUTHORIZED, problem, null);
    }
}

