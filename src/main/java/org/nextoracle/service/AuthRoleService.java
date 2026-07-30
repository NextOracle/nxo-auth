package org.nextoracle.service;

import org.nextoracle.dto.AuthRoleDto;
import org.nextoracle.entity.AuthRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link AuthRole}.
 */
public interface AuthRoleService {
    /**
     * Save a authRole.
     *
     * @param authRoleDto the entity to save.
     * @return the persisted entity.
     */
    AuthRoleDto save(AuthRoleDto authRoleDto);

    /**
     * Updates a authRole.
     *
     * @param authRoleDto the entity to update.
     * @return the persisted entity.
     */
    AuthRoleDto update(AuthRoleDto authRoleDto);

    /**
     * Partially updates a authRole.
     *
     * @param authRole the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AuthRole> partialUpdate(AuthRole authRole);

    /**
     * Get the "id" authRole.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthRoleDto> findOne(UUID id);

    AuthRole findById(UUID roleId);

    Page<AuthRoleDto> findAll(Pageable pageable);

    /**
     * Delete the "id" authRole.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);

    AuthRole saveEntity(AuthRole authRole);

    AuthRole getRoleByName(String roleName);
}
