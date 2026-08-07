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
     * Get all authUsers with their roles in a paginated format.
     *
     * @param pageable the pagination information.
     * @return a page of {@link AuthUserWithRolesDto} entities.
     */
    Page<AuthUserWithRolesDto> getUsersWithRoles(Pageable pageable);

    /**
     * Update the last login timestamp for a user.
     *
     * @param userId the id of the user.
     */
    void updateLastLogin(UUID userId);

    /**
     * Delete the "id" authUser.
     *
     * @param id the id of the user.
     */
    void delete(UUID id);

    /**
     * Get the currently authenticated user.
     *
     * @return the entity.
     */
    AuthUser findByUser();

    /**
     * Get a user by their email.
     *
     * @param email the email of the user.
     * @return an optional containing the user if found.
     */
    Optional<AuthUser> getUserByEmail(String email);

    /**
     * Get a user by their username.
     *
     * @param username the username of the user.
     * @return an optional containing the user if found.
     */
    Optional<AuthUser> getByUsername(String username);
}
