package org.nextoracle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.nextoracle.config.AppProperties;
import org.nextoracle.entity.AuthRole;
import org.nextoracle.entity.AuthRoleUser;
import org.nextoracle.entity.AuthUser;
import org.nextoracle.repository.AuthUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

/**
 * Seeds the initial admin user and role on application startup if they do not already exist.
 * Credentials are read from {@code nxo-auth.admin.*} in application.yml.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class AdminCreation implements ApplicationRunner {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final AppProperties appProperties;
    private final AuthUserRepository authUserRepository;
    private final AuthRoleService authRoleService;
    private final AuthRoleUserService authRoleUserService;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        AppProperties.AdminProperties admin = appProperties.getAdmin();

        if (admin.getUsername() == null || admin.getPassword() == null
                || admin.getRole() == null || admin.getMail() == null) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Admin credentials not configured. Set nxo-auth.admin.* in application.yml"), null);
        }

        // 1. Create role if it doesn't exist
        AuthRole role;
        try {
            role = authRoleService.getRoleByName(admin.getRole());
        } catch (Exception e) {
            log.info("Creating initial role: {}", admin.getRole());
            AuthRole newRole = new AuthRole();
            newRole.setArName(admin.getRole());
            newRole.setArDescription("Administrator role (auto-created)");
            role = authRoleService.saveEntity(newRole);
        }

        // 2. Create admin user if it doesn't exist
        if (!authUserRepository.existsByAuUsername(admin.getUsername())) {
            log.info("Creating initial admin user: {}", admin.getUsername());
            AuthUser user = new AuthUser();
            user.setAuUsername(admin.getUsername());
            user.setAuEmail(admin.getMail());
            user.setAuPassword(ENCODER.encode(admin.getPassword()));
            user.setAuIsVerified(Boolean.TRUE);
            authUserRepository.save(user);
        }

        // 3. Assign role to user if not already assigned
        AuthUser user = authUserRepository.findByAuUsername(admin.getUsername())
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Admin user not found after creation"), null));

        final AuthRole finalRole = role;
        boolean alreadyAssigned = authRoleUserService.findAllByUserId(user.getAuId())
                .stream()
                .anyMatch(ru -> ru.getAuthRole() != null
                        && finalRole.getArId().equals(ru.getAuthRole().getArId()));

        if (!alreadyAssigned) {
            log.info("Assigning role {} to user {}", admin.getRole(), admin.getUsername());
            AuthRoleUser roleUser = new AuthRoleUser();
            roleUser.setAuthUser(user);
            roleUser.setAuthRole(finalRole);
            authRoleUserService.assignRoleToUser(roleUser);
        }

        log.info("Admin setup complete. User '{}' with role '{}' is ready.", admin.getUsername(), admin.getRole());
    }
}
