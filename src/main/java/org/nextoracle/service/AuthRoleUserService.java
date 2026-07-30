package org.nextoracle.service;

import org.nextoracle.dto.AuthRoleUserDto;
import org.nextoracle.entity.AuthRoleUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link AuthRoleUser}.
 */
public interface AuthRoleUserService {
    /**
     * Save a authRoleUser.
     *
     * @param authRoleUserDTO the entity to save.
     * @return the persisted entity.
     */
    AuthRoleUserDto save(AuthRoleUserDto authRoleUserDTO);

    /**
     * Assigns a specific role to a user.
     *
     * @param authRoleUser the request containing user ID and role ID.
     * @return the persisted {@link AuthRoleUserDto} representing the assignment.
     */
    AuthRoleUserDto assignRoleToUser(AuthRoleUser authRoleUser);

    /**
     * Updates a authRoleUser.
     *
     * @param authRoleUserDTO the entity to update.
     * @return the persisted entity.
     */
    AuthRoleUserDto update(AuthRoleUserDto authRoleUserDTO);

    /**
     * Partially updates a authRoleUser.
     *
     * @param authRoleUser the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AuthRoleUser> partialUpdate(AuthRoleUser authRoleUser);

    /**
     * Get the "id" authRoleUser.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthRoleUserDto> findOne(UUID id);

    /**
     * Get all authRoleUser associations in a paginated format.
     *
     * @param pageable the pagination information.
     * @return a page of {@link AuthRoleUserDto} entities.
     */
    Page<AuthRoleUserDto> findAll(Pageable pageable);

    /**
     * Get an authRoleUser association by its database ID.
     *
     * @param roleUserId the ID of the role-user association.
     * @return the entity.
     * @throws java.util.NoSuchElementException if the entity is not found.
     */
    AuthRoleUserDto findByUserId(UUID roleUserId);

    /**
     * Get all role-user associations for a given user ID.
     *
     * @param userId the ID of the user.
     * @return a list of {@link AuthRoleUserDto} associated with the user.
     */
    List<AuthRoleUserDto> findAllByUserId(UUID userId);

    /**
     * Delete the "id" authRoleUser.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);

    /**
     * Deletes all role-user associations for a given user.
     *
     * @param userId the ID of the user whose role assignments should be deleted.
     */
    void deleteByUserId(UUID userId);

    /**
     * Deletes a specific role assignment for a given user.
     *
     * @param roleId the ID of the role to remove.
     * @param userId the ID of the user from whom the role will be removed.
     */
    void deleteByRoleIdAndUserId(UUID roleId, UUID userId);
}
