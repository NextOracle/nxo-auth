package org.nextoracle.service;

import org.nextoracle.dto.AuthUserDto;
import org.nextoracle.dto.AuthUserWithRolesDto;
import org.nextoracle.entity.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link AuthUser}.
 */
public interface AuthUserService {
    /**
     * Save a authUser.
     *
     * @param authUser the entity to save.
     * @return the persisted entity.
     */
    AuthUser save(AuthUser authUser);

    /**
     * Updates a authUser.
     *
     * @param authUserDTO the entity to update.
     * @return the persisted entity.
     */
    AuthUserDto update(AuthUserDto authUserDTO);

    /**
     * Partially updates a authUser.
     *
     * @param authUser the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AuthUser> partialUpdate(AuthUser authUser);

    /**
     * Get the "id" authUser.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthUserDto> findOne(UUID id);


    /**
     * Get all authUsers in a paginated format.
     *
     * @param pageable the pagination information.
     * @return a page of {@link AuthUserDto} entities.
     */
    Page<AuthUserDto> findAll(Pageable pageable);


    /**
     * Get the "id" authUser.
     *
     * @param userId the id of the user.
     * @return the entity.
     */
    AuthUser findById(UUID userId);

    /**
     *
     */
    Page<AuthUserWithRolesDto> getUsersWithRoles(Pageable pageable);

    /**
     * Update the last login timestamp for a user.
     */
    void updateLastLogin(UUID userId);

    /**
     * Delete the "id" authUser.
     */
    void delete(UUID id);

    /**
     *
     */
    AuthUser findByUser();

    Optional<AuthUser> getUserByEmail(String email);
}
