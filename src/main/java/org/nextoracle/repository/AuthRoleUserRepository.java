package org.nextoracle.repository;

import org.nextoracle.entity.AuthRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for the AuthRoleUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthRoleUserRepository extends JpaRepository<AuthRoleUser, UUID>, JpaSpecificationExecutor<AuthRoleUser> {

    List<AuthRoleUser> findAllByAuthUser_auId(UUID userId);

    void deleteByAuthUser_AuId(UUID authUserAuId);

    void deleteByAuthUser_AuId_AndAuthRole_ArId(UUID authUserAuId, UUID authRoleArId);
}
