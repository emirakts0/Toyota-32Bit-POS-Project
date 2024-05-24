package com.userservice.Service.impl;

import com.userservice.Service.UserManagementService;
import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.RoleDto;
import com.userservice.dto.UpdateRequestDto;
import com.userservice.exception.*;
import com.userservice.model.Employee;
import com.userservice.model.Role;
import com.userservice.repository.EmployeeRepository;
import com.userservice.repository.RoleRepository;
import jakarta.transaction.Transactional;
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

            validateDto(request, null);

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


    @Transactional
    @Override
    public UpdateRequestDto updateEmployeeById(Long id, UpdateRequestDto updateRequestDto) {

        validateDto(updateRequestDto, id);

        String password = null;

        if(updateRequestDto.getPassword() != null) {
            password = passwordEncoder.encode(updateRequestDto.getPassword());
            updateRequestDto.setPassword(password);
        }

        employeeRepository.updateEmployeeById(
                id,
                updateRequestDto.getUsername(),
                updateRequestDto.getEmail(),
                password,
                updateRequestDto.getName(),
                updateRequestDto.getSurname(),
                LocalDateTime.now());


        Set<RoleDto> dtoRoles = updateRequestDto.getRoles();

        if (dtoRoles != null && !dtoRoles.isEmpty()) {
            Employee existingEmployee = employeeRepository.findById(id)
                    .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)));

            Set<Role> existingRoles = new HashSet<>();
            for (RoleDto roleDto : updateRequestDto.getRoles()) {
                Optional<Role> existingRoleOptional = roleRepository.findByRoleName(roleDto.getRoleName());
                existingRoleOptional.ifPresent(existingRoles::add);
            }

            existingEmployee.setRoles(existingRoles);
            employeeRepository.save(existingEmployee);
        }

        return updateRequestDto;
    }



    @Transactional
    @Override
    public Long deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)));

        if (employee.isDeleted())
            throw new EmployeeAlreadyDeletedException(String.format("Employee with ID %d already deleted", id));

        employeeRepository.markAsDeleted(id);
        return id;
    }

    @Transactional
    @Override
    public Long reactivateEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)));

        if (!employee.isDeleted())
            throw new EmployeeAlreadyExistsException(String.format("Employee with ID %d already exists", id));

        employee.setDeleted(false);
        employeeRepository.save(employee);

        return id;
    }


//-----------------------------------------------------------------------------------------------------------------------
    private void validateDto(Object requestDto, Long id) {

        if (requestDto instanceof RegisterRequestDto registerRequest) {

            employeeRepository.findByUsername(registerRequest.getUsername())
                    .ifPresent(u -> {
                        throw new TakenUsernameException(String.format("Username: %s is taken", registerRequest.getUsername()));
                    });

            employeeRepository.findByEmail(registerRequest.getEmail())
                    .ifPresent(e -> {
                        throw new TakenEmailException(String.format("Email: %s is taken", registerRequest.getEmail()));
                    });

        }
        else if (requestDto instanceof UpdateRequestDto updateRequest) {

            Employee existingEmployee = employeeRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)));

            if (updateRequest.getUsername() != null) {
                if (updateRequest.getUsername().equals(existingEmployee.getUsername())) {
                    throw new TakenUsernameException(String.format("Username: %s is already your current username", updateRequest.getUsername()));
                } else {
                    employeeRepository.findByUsername(updateRequest.getUsername())
                            .ifPresent(u -> {
                                throw new TakenUsernameException(String.format("Username: %s is taken", updateRequest.getUsername()));
                            });
                }
            }
            if (updateRequest.getEmail() != null) {
                if (updateRequest.getEmail().equals(existingEmployee.getEmail())) {
                    throw new TakenEmailException(String.format("Email: %s is already your current email", updateRequest.getEmail()));
                } else {
                    employeeRepository.findByEmail(updateRequest.getEmail())
                            .ifPresent(e -> {
                                throw new TakenEmailException(String.format("Email: %s is taken", updateRequest.getEmail()));
                            });
                }
            }
        }
    }
}


