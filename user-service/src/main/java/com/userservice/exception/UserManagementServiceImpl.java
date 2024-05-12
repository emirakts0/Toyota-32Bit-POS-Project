package com.userservice.exception;

import com.user.Service.UserManagementService;
import com.user.dto.EmployeeDto;
import com.user.dto.RegisterRequestDto;
import com.user.dto.RoleDto;
import com.user.dto.UpdateRequestDto;
import com.user.exception.*;
import com.user.model.Employee;
import com.user.model.Role;
import com.user.repository.EmployeeRepository;
import com.user.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    //database 3 rolü ekle.,

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

    @Transactional
    @Override
    public EmployeeDto searchUserByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with username %s not found", username)));

        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Transactional
    @Override
    public Page<EmployeeDto> getAllUsersByFilterAndPagination(int pageSize, int pageNumber, boolean hideDeleted) {

        if (pageSize < 1 || pageNumber < 1)
            throw new InvalidInputException("page size and page number must be at least 1");

        Sort sort = Sort.by(Sort.Direction.DESC, "deleted").and(Sort.by(Sort.Direction.ASC, "username"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<Employee> employeePage = hideDeleted
                ? employeeRepository.findAllByDeletedFalse(pageable)
                : employeeRepository.findAll(pageable);

        return employeePage.map(employee -> modelMapper.map(employee, EmployeeDto.class));
    }



    //------------------------------------------------------------------------------------------------------------------
    private void validateDto(Object requestDto, Long id) {

        if (requestDto instanceof RegisterRequestDto) {
            RegisterRequestDto registerRequest = (RegisterRequestDto) requestDto;

            employeeRepository.findByUsername(registerRequest.getUsername())
                    .ifPresent(u -> {
                        throw new TakenUsernameException(String.format("Username: %s is taken", registerRequest.getUsername()));
                    });

            employeeRepository.findByEmail(registerRequest.getEmail())
                    .ifPresent(e -> {
                        throw new TakenEmailException(String.format("Email: %s is taken", registerRequest.getEmail()));
                    });

        }
        else if (requestDto instanceof UpdateRequestDto) {
            UpdateRequestDto updateRequest = (UpdateRequestDto) requestDto;

            employeeRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)));
            
            if (updateRequest.getUsername() != null) {
                employeeRepository.findByUsername(updateRequest.getUsername())
                        .ifPresent(u -> {
                            throw new TakenUsernameException(String.format("Username: %s is taken", updateRequest.getUsername()));
                        });
            }

            if (updateRequest.getEmail() != null) {
                employeeRepository.findByEmail(updateRequest.getEmail())
                        .ifPresent(e -> {
                            throw new TakenEmailException(String.format("Email: %s is taken", updateRequest.getEmail()));
                        });
            }
        }
    }
}


