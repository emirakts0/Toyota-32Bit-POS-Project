package com.user.Service.impl;

import com.user.dto.EmployeeDto;
import com.user.exception.EmployeeNotFoundException;
import com.user.exception.InvalidInputException;
import com.user.model.Employee;
import com.user.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSearchServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserSearchServiceImpl userSearchService;


    @Test
    void whenSearchEmployeeByUsernameWithValidUsername_thenReturnEmployeeDto() {
        String username = "validUsername";
        Employee employee = createEmployee(username, null, false);
        EmployeeDto employeeDto = createEmployeeDto(username, null);

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeDto.class)).thenReturn(employeeDto);

        EmployeeDto result = userSearchService.searchEmployeeByUsername(username);

        assertEmployeeDto(result, username, null);
        verify(employeeRepository, times(1)).findByUsername(username);
        verify(modelMapper, times(1)).map(employee, EmployeeDto.class);
    }

    @Test
    void whenSearchEmployeeByUsernameWithNonExistentUsername_thenThrowEmployeeNotFoundException() {
        String username = "nonExistentUsername";

        when(employeeRepository.findByUsername(username)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class, () -> userSearchService.searchEmployeeByUsername(username));

        assertEquals("Employee with username nonExistentUsername not found", exception.getMessage());
        verify(employeeRepository, times(1)).findByUsername(username);
        verify(modelMapper, never()).map(any(Employee.class), any(EmployeeDto.class));
    }

    @Test
    void whenGetEmployeeByIdWithValidId_thenReturnEmployeeDto() {
        Long id = 1L;
        Employee employee = createEmployee(null, id, false);
        EmployeeDto employeeDto = createEmployeeDto(null, id);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeDto.class)).thenReturn(employeeDto);

        EmployeeDto result = userSearchService.getEmployeeById(id);

        assertEmployeeDto(result, null, id);
        verify(employeeRepository, times(1)).findById(id);
        verify(modelMapper, times(1)).map(employee, EmployeeDto.class);
    }

    @Test
    void whenGetEmployeeByIdWithNonExistentId_thenThrowEmployeeNotFoundException() {
        Long id = 1L;

        when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class, () -> userSearchService.getEmployeeById(id));

        assertEquals("Employee with ID " + id + " not found", exception.getMessage());
        verify(employeeRepository, times(1)).findById(id);
        verify(modelMapper, never()).map(any(Employee.class), any(EmployeeDto.class));
    }

    @Test
    void whenGetAllEmployeesByFilterAndPaginationWithInvalidPageSize_thenThrowInvalidInputException() {
        assertInvalidInputException(0, 1, true, "Page size and page number must be at least 1");
    }

    @Test
    void whenGetAllEmployeesByFilterAndPaginationWithInvalidPageNumber_thenThrowInvalidInputException() {
        assertInvalidInputException(1, 0, true, "Page size and page number must be at least 1");
    }

    @Test
    void whenGetAllEmployeesByFilterAndPaginationWithHideDeletedTrue_thenReturnNonDeletedEmployees() {
        Employee employee1 = createEmployee(null, null, false);
        Employee employee2 = createEmployee(null, null, false);
        assertEmployeePage(1, 1, true, Arrays.asList(employee1, employee2), 2);
    }

    @Test
    void whenGetAllEmployeesByFilterAndPaginationWithHideDeletedFalse_thenReturnAllEmployees() {
        Employee employee1 = createEmployee(null, null, false);
        Employee employee2 = createEmployee(null, null, true);
        assertEmployeePage(1, 1, false, Arrays.asList(employee1, employee2), 2);
    }



    private Employee createEmployee(String username, Long id, boolean isDeleted) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setId(id);
        employee.setDeleted(isDeleted);
        return employee;
    }

    private EmployeeDto createEmployeeDto(String username, Long id) {
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setUsername(username);
        employeeDto.setId(id);
        return employeeDto;
    }

    private void assertEmployeeDto(EmployeeDto result, String expectedUsername, Long expectedId) {
        assertNotNull(result);
        assertEquals(expectedUsername, result.getUsername());
        assertEquals(expectedId, result.getId());
    }

    private void assertInvalidInputException(int pageSize, int pageNumber, boolean hideDeleted, String expectedMessage) {
        InvalidInputException exception = assertThrows(
                InvalidInputException.class, () -> userSearchService.getAllEmployeesByFilterAndPagination(pageSize, pageNumber, hideDeleted));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertEmployeePage(int pageSize, int pageNumber, boolean hideDeleted, List<Employee> employees, int expectedTotalElements) {
        Page<Employee> employeePage = new PageImpl<>(employees);

        if (hideDeleted) {
            when(employeeRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(employeePage);
        } else {
            when(employeeRepository.findAll(any(Pageable.class))).thenReturn(employeePage);
        }

        List<EmployeeDto> employeeDtos = employees.stream()
                .map(e -> createEmployeeDto(e.getUsername(), e.getId()))
                .toList();

        for (int i = 0; i < employees.size(); i++) {
            when(modelMapper.map(employees.get(i), EmployeeDto.class)).thenReturn(employeeDtos.get(i));
        }

        Page<EmployeeDto> result = userSearchService.getAllEmployeesByFilterAndPagination(pageSize, pageNumber, hideDeleted);

        assertNotNull(result);
        assertEquals(expectedTotalElements, result.getTotalElements());
        if (hideDeleted) {
            verify(employeeRepository, times(1)).findAllByDeletedFalse(any(Pageable.class));
        } else {
            verify(employeeRepository, times(1)).findAll(any(Pageable.class));
        }
    }
}
