package com.userservice.config;

import com.userservice.model.Role;
import com.userservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class StartupDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        List<Role> defaultRoles = Arrays.asList(
                new Role("ROLE_CASHIER"),
                new Role("ROLE_MANAGER"),
                new Role("ROLE_ADMIN")
        );

        for (Role role : defaultRoles) {

            Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
            if (existingRole.isEmpty()) {

                roleRepository.save(role);
            }
        }
    }

    public StartupDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
}