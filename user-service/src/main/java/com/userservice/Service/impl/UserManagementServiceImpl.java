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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public Set<RegisterRequestDto> registerEmployee(Set<RegisterRequestDto> registerRequestDtoSet) {
        log.trace("registerEmployee method begins. registerRequestDtoSet size: {}", registerRequestDtoSet.size());

        for (RegisterRequestDto request : registerRequestDtoSet) {
            log.debug("registerEmployee: Processing registration for username: {}", request.getUsername());
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
            log.info("registerEmployee: Registered employee, ID : {}, username : {}", employee.getId(), employee.getUsername());
        }
        log.info("registerEmployee: All employees registered, size: {}", registerRequestDtoSet.size());

        log.trace("registerEmployee method ends. registerRequestDtoSet size: {}", registerRequestDtoSet.size());
        return registerRequestDtoSet;
    }


    @Transactional
    @Override
    public UpdateRequestDto updateEmployeeById(Long id, UpdateRequestDto updateRequestDto) {
        log.trace("updateEmployeeById method begins. ID: {}", id);

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
            Employee existingEmployee = findAndCheckEmployeeById(id);

            Set<Role> existingRoles = new HashSet<>();
            for (RoleDto roleDto : updateRequestDto.getRoles()) {
                Optional<Role> existingRoleOptional = roleRepository.findByRoleName(roleDto.getRoleName());
                existingRoleOptional.ifPresent(existingRoles::add);
                log.debug("updateEmployeeById: Role {} found and added for user ID: {}", roleDto.getRoleName(), id);
            }

            existingEmployee.setRoles(existingRoles);
            employeeRepository.save(existingEmployee);
        }

        log.info("updateEmployeeById: updated employee, ID: {}", id);
        log.trace("updateEmployeeById method ends. ID: {}", id);
        return updateRequestDto;
    }


    @Transactional
    @Override
    public Long deleteEmployeeById(Long id) {
        log.trace("deleteEmployeeById method begins. ID: {}", id);

        Employee employee = findAndCheckEmployeeById(id);

        if (employee.isDeleted()) {
            log.warn("deleteEmployeeById: Attempted to delete employee with ID {} but already deleted", id);
            throw new EmployeeAlreadyDeletedException(String.format("Employee with ID %d already deleted", id));
        }

        employeeRepository.markAsDeleted(id);
        log.info("deleteEmployeeById: Marked employee as deleted, ID: {}", id);

        log.trace("deleteEmployeeById method ends. ID: {}", id);
        return id;
    }


    @Transactional
    @Override
    public Long reactivateEmployeeById(Long id) {
        log.trace("reactivateEmployeeById method begins. ID: {}", id);

        Employee employee = findAndCheckEmployeeById(id);

        if (!employee.isDeleted()) {
            log.warn("reactivateEmployeeById: Attempted to reactivate employee with ID {} but are not marked as deleted", id);
            throw new EmployeeAlreadyExistsException(String.format("Employee with ID %d already exists", id));
        }

        employee.setDeleted(false);
        employeeRepository.save(employee);
        log.info("reactivateEmployeeById: Reactivated employee, ID: {}", id);

        log.trace("reactivateEmployeeById method ends. ID: {}", id);
        return id;
    }


    //-----------------------------------------------------------------------------------------------------------------------
    private Employee findAndCheckEmployeeById(Long id) {
        log.trace("findAndCheckEmployeeById method begins. ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("findAndCheckEmployeeById: Employee with ID {} not found", id);
                    return new EmployeeNotFoundException(String.format("Employee with ID %d not found", id));
                });

        log.trace("findAndCheckEmployeeById method ends. ID: {}", id);
        return employee;
    }


    private void validateDto(Object requestDto, Long id) {
        log.trace("validateDto method begins.");

        if (requestDto instanceof RegisterRequestDto registerRequest) {
            registerReqDtoControl(registerRequest);
        }
        else if (requestDto instanceof UpdateRequestDto updateRequest) {
            updateReqDtoControl(id, updateRequest);
        }

        log.trace("validateDto method ends.");
    }


    private void registerReqDtoControl(RegisterRequestDto registerRequest) {
        log.trace("registerReqDtoControl method begins for username: {}", registerRequest.getUsername());

        employeeRepository.findByUsername(registerRequest.getUsername())
                .ifPresent(u -> {
                    log.warn("registerReqDtoControl: Username: {} is already taken", registerRequest.getUsername());
                    throw new TakenUsernameException(String.format("Username: %s is taken", registerRequest.getUsername())); });

        employeeRepository.findByEmail(registerRequest.getEmail())
                .ifPresent(e -> {
                    log.warn("registerReqDtoControl: Email: {} is already taken", registerRequest.getEmail());
                    throw new TakenEmailException(String.format("Email: %s is taken", registerRequest.getEmail())); });

        log.trace("registerReqDtoControl method ends for username: {}", registerRequest.getUsername());
    }


    private void updateReqDtoControl(Long id, UpdateRequestDto updateRequest) {
        log.trace("updateReqDtoControl method begins for employee ID: {}", id);

        Employee existingEmployee = employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("updateReqDtoControl: Employee with ID {} not found", id);
                    return new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)); });

        if (updateRequest.getUsername() != null) {

            if (updateRequest.getUsername().equals(existingEmployee.getUsername())) {
                log.warn("updateReqDtoControl: Username: {} is already the current username for employee with ID {}", updateRequest.getUsername(), id);
                throw new TakenUsernameException(String.format("Username: %s is already your current username", updateRequest.getUsername()));

            } else {
                employeeRepository.findByUsername(updateRequest.getUsername())
                        .ifPresent(u -> {
                            log.warn("updateReqDtoControl: Username: {} is already taken", updateRequest.getUsername());
                            throw new TakenUsernameException(String.format("Username: %s is taken", updateRequest.getUsername())); });
            }
        }

        if (updateRequest.getEmail() != null) {
            if (updateRequest.getEmail().equals(existingEmployee.getEmail())) {
                log.warn("updateReqDtoControl: Email: {} is already the current email for employee with ID {}", updateRequest.getEmail(), id);
                throw new TakenEmailException(String.format("Email: %s is already your current email", updateRequest.getEmail()));
            } else {
                employeeRepository.findByEmail(updateRequest.getEmail())
                        .ifPresent(e -> {
                            log.warn("updateReqDtoControl: Email: {} is already taken", updateRequest.getEmail());
                            throw new TakenEmailException(String.format("Email: %s is taken", updateRequest.getEmail()));
                        });
            }
        }

        log.trace("updateReqDtoControl method ends for employee ID: {}", id);
    }

}
