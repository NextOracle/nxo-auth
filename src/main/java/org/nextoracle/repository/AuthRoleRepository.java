package org.nextoracle.repository;

import org.nextoracle.entity.AuthRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the AuthRole entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthRoleRepository extends JpaRepository<AuthRole, UUID>, JpaSpecificationExecutor<AuthRole> {

    Optional<AuthRole> findByArName(String name);
}
