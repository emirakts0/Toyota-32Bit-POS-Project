package com.security.service.impl;

import com.security.exception.UserNotFoundException;
import com.security.model.Employee;
import com.security.model.Role;
import com.security.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void whenLoadUserByUsernameWithValidUsername_thenReturnUserDetails() {
        String username = "validUser";
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword("validPassword");

        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setRoleName("USER");
        roles.add(role);
        employee.setRoles(roles);

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.of(employee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("validPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));

        verify(employeeRepository, times(1)).findByUsername(username);
    }

    @Test
    void whenLoadUserByUsernameWithInvalidUsername_thenThrowUsernameNotFoundException() {
        String username = "invalidUser";

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username));

        assertEquals("User not found with username: " + username, exception.getMessage());

        verify(employeeRepository, times(1)).findByUsername(username);
    }


    @Test
    void whenGetNameByUsernameWithValidUsername_thenReturnName() {
        String username = "validUser";
        String name = "John Doe";
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setName(name);

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.of(employee));

        String result = customUserDetailsService.getNameByUsername(username);

        assertEquals(name, result);
        verify(employeeRepository, times(1)).findByUsername(username);
    }

    @Test
    void whenGetNameByUsernameWithInvalidUsername_thenReturnUnderscore() {
        String username = "invalidUser";

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.empty());

        String result = customUserDetailsService.getNameByUsername(username);

        assertEquals("_", result);
        verify(employeeRepository, times(1)).findByUsername(username);
    }
}