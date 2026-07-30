package org.nextoracle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.nextoracle.AppRole;
import org.nextoracle.entity.AuthRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the default application roles on startup if they do not already exist.
 * Users are created via OAuth2 / WebAuthn providers - no password-based seeding.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AdminCreation implements ApplicationRunner {

    private final AuthRoleService authRoleService;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        for (AppRole appRole : AppRole.values()) {
            String roleName = appRole.name();
            try {
                authRoleService.getRoleByName(roleName);
            } catch (Exception _) {
                AuthRole role = new AuthRole();
                role.setArName(roleName);
                role.setArDescription(roleName + " role (auto-created)");
                authRoleService.saveEntity(role);
                log.info("Created role: {}", roleName);
            }
        }
        log.info("Role seeding completed.");
    }
}
