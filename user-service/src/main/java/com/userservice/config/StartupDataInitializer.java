package com.userservice.config;

import com.userservice.model.Employee;
import com.userservice.model.Role;
import com.userservice.repository.EmployeeRepository;
import com.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.trace("StartupDataInitializer run method begins.");

        List<Role> defaultRoles = Arrays.asList(
                new Role("CASHIER"),
                new Role("MANAGER"),
                new Role("ADMIN")
        );

        for (Role role : defaultRoles) {
            log.debug("Checking if role {} exists.", role.getRoleName());

            Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
            if (existingRole.isEmpty()) {
                log.info("Role {} does not exist. Saving to repository.", role.getRoleName());
                roleRepository.save(role);
            } else {
                log.debug("Role {} already exists. Skipping save.", role.getRoleName());
            }
        }

        //------------------------------------------------------------------------------------------

        Optional<Employee> existingAdmin = employeeRepository.findByUsername("admin");
        if (existingAdmin.isEmpty()) {
            log.info("Admin user does not exist. Creating new admin user.");

            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByRoleName("ADMIN").ifPresent(adminRoles::add);

            Employee admin = new Employee();

            admin.setId(0L);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@admin.com");
            admin.setName("Admin");
            admin.setSurname("Default");
            admin.setRoles(adminRoles);
            admin.setRegistrationDate(LocalDateTime.now());

            employeeRepository.save(admin);
            log.info("Admin user created with username: admin");
        } else {
            log.debug("Admin user already exists. Skipping creation.");
        }

        log.trace("StartupDataInitializer run method ends.");
    }
}