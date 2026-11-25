package com.milkcheck.config;

import com.milkcheck.entity.Role;
import com.milkcheck.entity.User;
import com.milkcheck.repository.RoleRepository;
import com.milkcheck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role customerRole = findOrCreateRole("ROLE_CUSTOMER");
        Role adminRole = findOrCreateRole("ROLE_ADMIN");

        String email = adminEmail.trim().toLowerCase();
        User admin = userRepository.findByEmail(email).orElseGet(User::new);
        boolean isNew = admin.getId() == null;

        admin.setFullName(adminName);
        admin.setEmail(email);
        admin.setEnabled(true);
        if (isNew || !passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
        }
        admin.getRoles().add(adminRole);
        admin.getRoles().add(customerRole);
        userRepository.save(admin);

        log.info("Admin account ready: {} (created={})", email, isNew);
    }

    private Role findOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(null, name)));
    }
}