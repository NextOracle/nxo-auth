package org.nextoracle.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.dto.AuthRoleUserDto;
import org.nextoracle.entity.AuthRole;
import org.nextoracle.entity.AuthRoleUser;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.mapper.AuthRoleUserMapper;
import org.nextoracle.repository.AuthRoleUserRepository;
import org.nextoracle.service.AuthRoleService;
import org.nextoracle.service.AuthRoleUserService;
import org.nextoracle.service.AuthUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Implementation for managing {@link AuthRoleUser}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class AuthRoleUserServiceImpl implements AuthRoleUserService {

    private final AuthRoleUserRepository authRoleUserRepository;

    private final AuthRoleUserMapper authRoleUserMapper;

    private final AuthUserService authUserService;

    private final AuthRoleService authRoleService;

    @Transactional
    @Override
    public AuthRoleUserDto save(AuthRoleUserDto authRoleUserDto) {
        AuthRoleUser authRoleUser = authRoleUserMapper.toEntity(authRoleUserDto);
        authRoleUser = authRoleUserRepository.save(authRoleUser);
        return authRoleUserMapper.toDto(authRoleUser);
    }

    @Transactional
    @Override
    public AuthRoleUserDto assignRoleToUser(AuthRoleUser authRoleUser) {
        final AuthUser user = authUserService.findById(authRoleUser.getAuthUser().getAuId());
        final AuthRole role = authRoleService.findById(authRoleUser.getAuthRole().getArId());
        final AuthRoleUser updatedAuthRoleUser = new AuthRoleUser();
        updatedAuthRoleUser.setAuthUser(user);
        updatedAuthRoleUser.setAuthRole(role);
        return authRoleUserMapper.toDto(authRoleUserRepository.save(updatedAuthRoleUser));
    }

    @Transactional
    @Override
    public AuthRoleUserDto update(AuthRoleUserDto authRoleUserDto) {
        AuthRoleUser authRoleUser = authRoleUserMapper.toEntity(authRoleUserDto);
        authRoleUser = authRoleUserRepository.save(authRoleUser);
        return authRoleUserMapper.toDto(authRoleUser);
    }

    @Transactional
    @Override
    public Optional<AuthRoleUser> partialUpdate(AuthRoleUser authRoleUser) {

        return authRoleUserRepository
                .findById(authRoleUser.getAruId())
                .map(existingAuthRoleUser -> {
                    authRoleUserMapper.partialUpdate(existingAuthRoleUser, authRoleUser);

                    return existingAuthRoleUser;
                })
                .map(authRoleUserRepository::save);
    }

    @Override
    public Optional<AuthRoleUserDto> findOne(UUID id) {
        return authRoleUserRepository.findById(id).map(authRoleUserMapper::toDto);
    }

    @Override
    public Page<AuthRoleUserDto> findAll(Pageable pageable) {
        return authRoleUserRepository.findAll(pageable).map(authRoleUserMapper::toDto);
    }

    @Override
    public AuthRoleUserDto findByUserId(UUID roleUserId) {
        return authRoleUserRepository.findById(roleUserId).map(authRoleUserMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("No AuthRoleUser found with id " + roleUserId));
    }

    @Override
    public List<AuthRoleUserDto> findAllByUserId(UUID userId) {
        return authRoleUserMapper.toDto(authRoleUserRepository.findAllByAuthUser_auId(userId));
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        authRoleUserRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        authRoleUserRepository.deleteByAuthUser_AuId(userId);
    }

    @Override
    public void deleteByRoleIdAndUserId(UUID roleId, UUID userId) {
        authRoleUserRepository.deleteByAuthUser_AuId_AndAuthRole_ArId(userId, roleId);
    }
}
