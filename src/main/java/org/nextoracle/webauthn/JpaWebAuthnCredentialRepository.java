package org.nextoracle.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.Base64UrlException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.entity.WebAuthnCredential;
import org.nextoracle.repository.AuthUserRepository;
import org.nextoracle.repository.WebAuthnCredentialRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class JpaWebAuthnCredentialRepository implements CredentialRepository {

    private final AuthUserRepository authUserRepository;
    private final WebAuthnCredentialRepository credentialRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return authUserRepository.findByAuUsername(username)
                .map(user -> credentialRepository.findAllByUserId(user.getAuId()))
                .orElseGet(java.util.Collections::emptyList)
                .stream()
                .map(this::toDescriptor)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return authUserRepository.findByAuUsername(username)
                .map(user -> WebAuthnIds.toUserHandle(user.getAuId()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return authUserRepository.findById(WebAuthnIds.toUserId(userHandle))
                .map(u -> u.getAuUsername());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialRepository.findByCredentialId(credentialId.getBase64Url())
                .map(this::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return credentialRepository.findByCredentialId(credentialId.getBase64Url())
                .map(this::toRegisteredCredential)
                .map(Set::of)
                .orElseGet(Set::of);
    }

    private PublicKeyCredentialDescriptor toDescriptor(WebAuthnCredential credential) {
        PublicKeyCredentialDescriptor.PublicKeyCredentialDescriptorBuilder builder =
                PublicKeyCredentialDescriptor.builder()
                        .id(decode(credential.getCredentialId()));
        Set<AuthenticatorTransport> transports = parseTransports(credential.getTransports());
        if (!transports.isEmpty()) {
            builder.transports(transports);
        }
        return builder.build();
    }

    private RegisteredCredential toRegisteredCredential(WebAuthnCredential credential) {
        return RegisteredCredential.builder()
                .credentialId(decode(credential.getCredentialId()))
                .userHandle(WebAuthnIds.toUserHandle(credential.getUserId()))
                .publicKeyCose(new ByteArray(credential.getPublicKeyCose()))
                .signatureCount(credential.getSignatureCount())
                .build();
    }

    private Set<AuthenticatorTransport> parseTransports(String transports) {
        if (transports == null || transports.isBlank()) return Set.of();
        return Arrays.stream(transports.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(AuthenticatorTransport::of)
                .collect(Collectors.toSet());
    }

    private ByteArray decode(String base64Url) {
        try {
            return ByteArray.fromBase64Url(base64Url);
        } catch (Base64UrlException e) {
            throw new IllegalStateException("Stored credential id is not valid Base64URL: " + base64Url, e);
        }
    }
}
