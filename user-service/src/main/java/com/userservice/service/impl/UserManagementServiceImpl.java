package com.userservice.service.impl;

import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.RoleDto;
import com.userservice.model.Employee;
import com.userservice.model.Role;
import com.userservice.repository.EmployeeRepository;
import com.userservice.repository.RoleRepository;
import com.userservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@RequiredArgsConstructor
@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Set<RegisterRequestDto> registerEmployee(Set<RegisterRequestDto> registerRequestDtoSet) {
        for (RegisterRequestDto request : registerRequestDtoSet) {

            request.setPassword(passwordEncoder.encode(request.getPassword()));

            Set<Role> existingRoles = new HashSet<>();
            for (RoleDto roleDto : request.getRoles()) {
                Optional<Role> existingRoleOptional = roleRepository.findByRoleName(roleDto.getRoleName());
                existingRoleOptional.ifPresent(existingRoles::add);
            }

            Employee employee = modelMapper.map(request, Employee.class);
            employee.setRoles(existingRoles);
            employee.setRegistrationDate(LocalDateTime.now());
            employeeRepository.save(employee);
        }

        return registerRequestDtoSet;
    }



}
