package com.userservice.Service.impl;

import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.RoleDto;
import com.userservice.dto.UpdateRequestDto;
import com.userservice.exception.*;
import com.userservice.model.Employee;
import com.userservice.model.Role;
import com.userservice.repository.EmployeeRepository;
import com.userservice.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserManagementServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;


    @Test
    void whenRegisterEmployeeWithValidData_thenEmployeeShouldBeRegisteredSuccessfully() {
        RegisterRequestDto requestDto = createRegisterRequestDto("validUser", "validPassword", "valid@example.com", "ADMIN");
        Set<RegisterRequestDto> requestDtoSet = new HashSet<>();
        requestDtoSet.add(requestDto);

        Role role = new Role();
        role.setRoleName("ADMIN");

        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("validPassword")).thenReturn("encodedPassword");
        Employee employee = new Employee();
        when(modelMapper.map(requestDto, Employee.class)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Set<RegisterRequestDto> result = userManagementService.registerEmployee(requestDtoSet);

        assertEquals(1, result.size());
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    void whenRegisterEmployeeWithTakenUsername_thenThrowTakenUsernameException() {
        RegisterRequestDto requestDto = createRegisterRequestDto("takenUsername", "password", "email@example.com", "CASHIER");
        Set<RegisterRequestDto> requestDtoSet = new HashSet<>();
        requestDtoSet.add(requestDto);

        when(employeeRepository.findByUsername("takenUsername")).thenReturn(Optional.of(new Employee()));

        TakenUsernameException exception = assertThrows(TakenUsernameException.class, () -> {
            userManagementService.registerEmployee(requestDtoSet);
        });

        assertEquals("Username: takenUsername is taken", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void whenRegisterEmployeeWithTakenEmail_thenThrowTakenEmailException() {
        RegisterRequestDto requestDto = createRegisterRequestDto("username", "password", "takenEmail@example.com", "ADMIN");
        Set<RegisterRequestDto> requestDtoSet = new HashSet<>();
        requestDtoSet.add(requestDto);

        when(employeeRepository.findByEmail("takenEmail@example.com")).thenReturn(Optional.of(new Employee()));

        TakenEmailException exception = assertThrows(TakenEmailException.class, () -> {
            userManagementService.registerEmployee(requestDtoSet);
        });

        assertEquals("Email: takenEmail@example.com is taken", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void whenUpdateEmployeeByIdWithValidData_thenEmployeeShouldBeUpdatedSuccessfully() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setUsername("updatedUsername");
        updateRequestDto.setEmail("updatedEmail@example.com");
        updateRequestDto.setPassword("updatedPassword");

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));
        when(passwordEncoder.encode("updatedPassword")).thenReturn("encodedUpdatedPassword");

        userManagementService.updateEmployeeById(id, updateRequestDto);

        verify(employeeRepository, times(1)).updateEmployeeById(
                eq(id),
                eq("updatedUsername"),
                eq("updatedEmail@example.com"),
                eq("encodedUpdatedPassword"),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        );
    }

    @Test
    void whenUpdateEmployeeByIdWithNonExistentEmployee_thenThrowEmployeeNotFoundException() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            userManagementService.updateEmployeeById(id, updateRequestDto);
        });

        assertEquals("Employee with ID " + id + " not found", exception.getMessage());
        verify(employeeRepository, never()).updateEmployeeById(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void whenUpdateEmployeeByIdWithTakenUsername_thenThrowTakenUsernameException() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setUsername("takenUsername");

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findByUsername("takenUsername")).thenReturn(Optional.of(new Employee()));

        TakenUsernameException exception = assertThrows(TakenUsernameException.class, () -> {
            userManagementService.updateEmployeeById(id, updateRequestDto);
        });

        assertEquals("Username: takenUsername is taken", exception.getMessage());
        verify(employeeRepository, never()).updateEmployeeById(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void whenUpdateEmployeeByIdWithTakenEmail_thenThrowTakenEmailException() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setEmail("takenEmail@example.com");

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findByEmail("takenEmail@example.com")).thenReturn(Optional.of(new Employee()));

        TakenEmailException exception = assertThrows(TakenEmailException.class, () -> {
            userManagementService.updateEmployeeById(id, updateRequestDto);
        });

        assertEquals("Email: takenEmail@example.com is taken", exception.getMessage());
        verify(employeeRepository, never()).updateEmployeeById(
                anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void whenUpdateEmployeeByIdWithoutPassword_thenPasswordShouldRemainNull() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setUsername("updatedUsername");
        updateRequestDto.setEmail("updatedEmail@example.com");
        updateRequestDto.setPassword(null);

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));

        userManagementService.updateEmployeeById(id, updateRequestDto);

        verify(employeeRepository, times(1)).updateEmployeeById(
                eq(id),
                eq("updatedUsername"),
                eq("updatedEmail@example.com"),
                isNull(),
                isNull(),
                isNull(),
                any(LocalDateTime.class)
        );
    }

    @Test
    void whenUpdateEmployeeByIdWithValidRoles_thenRolesShouldBeUpdatedSuccessfully() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setUsername("updatedUsername");
        updateRequestDto.setEmail("updatedEmail@example.com");
        updateRequestDto.setPassword("updatedPassword");

        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName("ADMIN");
        Set<RoleDto> roles = new HashSet<>();
        roles.add(roleDto);
        updateRequestDto.setRoles(roles);

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);
        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));
        when(passwordEncoder.encode("updatedPassword")).thenReturn("encodedUpdatedPassword");
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));

        userManagementService.updateEmployeeById(id, updateRequestDto);

        assertEquals(1, existingEmployee.getRoles().size());
        assertTrue(existingEmployee.getRoles().contains(adminRole));

        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void whenUpdateEmployeeByIdWithSameUsername_thenThrowTakenUsernameException() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setUsername("currentUsername");

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));

        TakenUsernameException exception = assertThrows(TakenUsernameException.class, () -> {
            userManagementService.updateEmployeeById(id, updateRequestDto);
        });

        assertEquals("Username: currentUsername is already your current username", exception.getMessage());
    }

    @Test
    void whenUpdateEmployeeByIdWithSameEmail_thenThrowTakenEmailException() {
        Long id = 1L;
        UpdateRequestDto updateRequestDto = new UpdateRequestDto();
        updateRequestDto.setEmail("currentEmail@example.com");

        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(existingEmployee));

        TakenEmailException exception = assertThrows(TakenEmailException.class, () -> {
            userManagementService.updateEmployeeById(id, updateRequestDto);
        });

        assertEquals("Email: currentEmail@example.com is already your current email", exception.getMessage());
    }

    @Test
    void whenDeleteEmployeeByIdWithValidId_thenEmployeeShouldBeMarkedAsDeleted() {
        Long id = 1L;
        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        Long result = userManagementService.deleteEmployeeById(id);

        assertEquals(id, result);
        verify(employeeRepository, times(1)).markAsDeleted(id);
    }

    @Test
    void whenDeleteEmployeeByIdWithAlreadyDeletedEmployee_thenThrowEmployeeAlreadyDeletedException() {
        Long id = 1L;
        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", true);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        EmployeeAlreadyDeletedException exception = assertThrows(EmployeeAlreadyDeletedException.class, () -> {
            userManagementService.deleteEmployeeById(id);
        });

        assertEquals("Employee with ID " + id + " already deleted", exception.getMessage());
        verify(employeeRepository, never()).markAsDeleted(id);
    }

    @Test
    void whenDeleteEmployeeByIdWithNonExistentEmployee_thenThrowEmployeeNotFoundException() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            userManagementService.deleteEmployeeById(id);
        });

        assertEquals("Employee with ID " + id + " not found", exception.getMessage());
        verify(employeeRepository, never()).markAsDeleted(id);
    }

    @Test
    void whenReactivateEmployeeByIdWithValidId_thenEmployeeShouldBeReactivated() {
        Long id = 1L;
        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", true);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        Long result = userManagementService.reactivateEmployeeById(id);

        assertEquals(id, result);
        assertFalse(existingEmployee.isDeleted());
        verify(employeeRepository, times(1)).save(existingEmployee);
    }

    @Test
    void whenReactivateEmployeeByIdWithNotDeletedEmployee_thenThrowEmployeeAlreadyExistsException() {
        Long id = 1L;
        Employee existingEmployee = createEmployee(id, "currentUsername", "currentEmail@example.com", false);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));

        EmployeeAlreadyExistsException exception = assertThrows(EmployeeAlreadyExistsException.class, () -> {
            userManagementService.reactivateEmployeeById(id);
        });

        assertEquals("Employee with ID " + id + " already exists", exception.getMessage());
        verify(employeeRepository, never()).save(existingEmployee);
    }

    @Test
    void whenReactivateEmployeeByIdWithNonExistentEmployee_thenThrowEmployeeNotFoundException() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            userManagementService.reactivateEmployeeById(id);
        });

        assertEquals("Employee with ID " + id + " not found", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }


    private RegisterRequestDto createRegisterRequestDto(String username, String password, String email, String roleName) {
        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setUsername(username);
        requestDto.setPassword(password);
        requestDto.setEmail(email);
        Set<RoleDto> roles = new HashSet<>();
        RoleDto roleDto = new RoleDto();
        roleDto.setRoleName(roleName);
        roles.add(roleDto);
        requestDto.setRoles(roles);
        return requestDto;
    }

    private Employee createEmployee(Long id, String username, String email, boolean deleted) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setDeleted(deleted);
        return employee;
    }
}