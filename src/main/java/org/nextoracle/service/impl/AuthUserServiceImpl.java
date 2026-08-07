package org.nextoracle.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.dto.AuthUserDto;
import org.nextoracle.dto.AuthUserWithRolesDto;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.jwt.JwtService;
import org.nextoracle.mapper.AuthUserMapper;
import org.nextoracle.repository.AuthUserRepository;
import org.nextoracle.service.AuthUserService;
import org.nextoracle.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Implementation for managing {@link AuthUser}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;

    private final AuthUserMapper authUserMapper;

    private final JwtService jwtService;


    @Transactional
    @Override
    public AuthUser save(AuthUser authUser) {
        return authUserRepository.save(authUser);
    }

    @Transactional
    @Override
    public AuthUserDto update(AuthUserDto authUserDto) {
        log.debug("Request to update AuthUser : {}", authUserDto);
        AuthUser authUser = authUserMapper.toEntity(authUserDto);
        authUser = authUserRepository.save(authUser);
        return authUserMapper.toDto(authUser);
    }

    @Override
    public Optional<AuthUserDto> findOne(UUID id) {
        log.debug("Request to get AuthUser : {}", id);
        return authUserRepository.findById(id).map(authUserMapper::toDto);
    }

    @Override
    public Page<AuthUserDto> findAll(Pageable pageable) {
        return authUserRepository.findAll(pageable).map(authUserMapper::toDto);
    }

    @Transactional
    @Override
    public Optional<AuthUser> partialUpdate(AuthUser authUser) {
        log.debug("Request to partially update AuthUser : {}", authUser);

        return authUserRepository
                .findById(authUser.getAuId())
                .map(existingAuthUser -> {
                    authUserMapper.partialUpdate(existingAuthUser, authUser);

                    return existingAuthUser;
                })
                .map(authUserRepository::save);
    }


    @Override
    public AuthUser findById(UUID userId) {
        return authUserRepository.findById(userId)
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.NOT_FOUND,
                        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "User not found"), null));
    }

    @Override
    public Page<AuthUserWithRolesDto> getUsersWithRoles(Pageable pageable) {
        log.debug("Request to get all AuthUsers with roles");
        return authUserRepository.findAllUsersWithRoles(pageable);
    }

    @Transactional
    @Override
    public void updateLastLogin(UUID userId) {
        authUserRepository.findById(userId).ifPresent(user -> {
            user.setAuLastLogin(LocalDateTime.now(java.time.ZoneId.of("Europe/Athens")));
            authUserRepository.save(user);
        });
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        log.debug("Request to delete AuthUser : {}", id);
        authUserRepository.deleteById(id);
    }

    @Override
    public AuthUser findByUser() {
        return authUserRepository.findById(Objects.requireNonNull(SecurityUtil.getActiveUUID())).orElseThrow(() ->
                new ErrorResponseException(HttpStatus.NOT_FOUND, ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                        "User not found"), null));
    }

    @Override
    public Optional<AuthUser> getUserByEmail(String email) {
        return authUserRepository.findByAuEmail(email);
    }

    @Override
    public Optional<AuthUser> getByUsername(String username) {
        return authUserRepository.findByAuUsername(username);
    }
}