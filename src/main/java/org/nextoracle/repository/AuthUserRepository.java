package org.nextoracle.repository;

import org.nextoracle.dto.AuthUserWithRolesDto;
import org.nextoracle.entity.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the AuthUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, UUID>, JpaSpecificationExecutor<AuthUser> {

    Optional<AuthUser> findByAuUsername(String username);

    boolean existsByAuUsername(String username);

    @Query("""
            select new org.nextoracle.dto.AuthUserWithRolesDto(
                u.auId,
                u.auUsername,
                u.auCreatedAt,
                u.auLastLogin,
                cast(function('string_agg', r.arName, ',') as String),
                u.auEmail
            )
            from AuthUser u
            left join AuthRoleUser ru on ru.authUser.auId = u.auId
            left join AuthRole r on r.arId = ru.authRole.arId
            group by u.auId
            """)
    Page<AuthUserWithRolesDto> findAllUsersWithRoles(Pageable pageable);

    Optional<AuthUser> findByAuEmail(String auEmail);
}
