package org.nextoracle.repository;

import org.nextoracle.entity.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, UUID> {

    List<WebAuthnCredential> findAllByUserId(UUID userId);

    Optional<WebAuthnCredential> findByCredentialId(String credentialId);

    boolean existsByCredentialId(String credentialId);

    Optional<WebAuthnCredential> findByIdAndUserId(UUID id, UUID userId);
}

