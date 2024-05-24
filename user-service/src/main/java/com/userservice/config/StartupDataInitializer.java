package com.userservice.config;

import com.userservice.model.Employee;
import com.userservice.model.Role;
import com.userservice.repository.EmployeeRepository;
import com.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class StartupDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        List<Role> defaultRoles = Arrays.asList(
                new Role("CASHIER"),
                new Role("MANAGER"),
                new Role("ADMIN")
        );

        for (Role role : defaultRoles) {

            Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
            if (existingRole.isEmpty()) {

                roleRepository.save(role);
            }
        }

        //------------------------------------------------------------------------------------------

        Optional<Employee> existingAdmin = employeeRepository.findByUsername("admin");
        if (existingAdmin.isEmpty()) {
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
        }
    }
}