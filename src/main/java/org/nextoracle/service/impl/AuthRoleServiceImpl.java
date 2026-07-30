package org.nextoracle.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nextoracle.dto.AuthRoleDto;
import org.nextoracle.entity.AuthRole;
import org.nextoracle.mapper.AuthRoleMapper;
import org.nextoracle.repository.AuthRoleRepository;
import org.nextoracle.service.AuthRoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service Implementation for managing {@link AuthRole}.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class AuthRoleServiceImpl implements AuthRoleService {

    private final AuthRoleRepository authRoleRepository;

    private final AuthRoleMapper authRoleMapper;

    @Transactional
    @Override
    public AuthRoleDto save(AuthRoleDto authRoleDto) {
        AuthRole authRole = authRoleMapper.toEntity(authRoleDto);
        authRole = authRoleRepository.save(authRole);
        return authRoleMapper.toDto(authRole);
    }

    @Transactional
    @Override
    public AuthRoleDto update(AuthRoleDto authRoleDto) {
        AuthRole authRole = authRoleMapper.toEntity(authRoleDto);
        authRole = authRoleRepository.save(authRole);
        return authRoleMapper.toDto(authRole);
    }

    @Transactional
    @Override
    public Optional<AuthRole> partialUpdate(AuthRole authRole) {

        return authRoleRepository
                .findById(authRole.getArId())
                .map(existingAuthRole -> {
                    authRoleMapper.partialUpdate(existingAuthRole, authRole);

                    return existingAuthRole;
                })
                .map(authRoleRepository::save);
    }

    @Override
    public Optional<AuthRoleDto> findOne(UUID id) {
        return authRoleRepository.findById(id).map(authRoleMapper::toDto);
    }

    @Override
    public AuthRole findById(UUID roleId) {
        return authRoleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("No role found with this id"));
    }

    @Override
    public Page<AuthRoleDto> findAll(Pageable pageable) {
        return authRoleRepository.findAll(pageable).map(authRoleMapper::toDto);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        authRoleRepository.deleteById(id);
    }

    @Override
    public AuthRole saveEntity(AuthRole authRole) {
        return authRoleRepository.save(authRole);
    }

    @Override
    public AuthRole getRoleByName(String roleName) {
        return authRoleRepository.findByArName(roleName).orElseThrow(()
                -> new EntityNotFoundException("No role found with this name"));
    }
}
