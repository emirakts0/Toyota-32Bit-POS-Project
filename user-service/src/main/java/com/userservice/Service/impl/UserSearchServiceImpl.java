package com.userservice.Service.impl;

import com.userservice.Service.UserSearchService;
import com.userservice.dto.EmployeeDto;
import com.userservice.exception.EmployeeNotFoundException;
import com.userservice.exception.InvalidInputException;
import com.userservice.model.Employee;
import com.userservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;


    @Override
    public EmployeeDto searchEmployeeByUsername(String username) {
        log.trace("searchEmployeeByUsername method begins. Username: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("searchEmployeeByUsername: Employee with username {} not found", username);
                    return new EmployeeNotFoundException(String.format("Employee with username %s not found", username)); });

        EmployeeDto employeeDto = modelMapper.map(employee, EmployeeDto.class);

        log.trace("searchEmployeeByUsername method ends. employeeDto: {}", employeeDto);
        return employeeDto;
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        log.trace("getEmployeeById method begins. ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("getEmployeeById: Employee with ID {} not found", id);
                    return new EmployeeNotFoundException(String.format("Employee with ID %d not found", id)); });

        EmployeeDto employeeDto = modelMapper.map(employee, EmployeeDto.class);

        log.trace("getEmployeeById method ends. employeeDto: {}", employeeDto);
        return employeeDto;
    }

    @Override
    public Page<EmployeeDto> getAllEmployeesByFilterAndPagination(int pageSize, int pageNumber, boolean hideDeleted) {
        log.trace("getAllEmployeesByFilterAndPagination method begins. PageSize: {}, PageNumber: {}, HideDeleted: {}", pageSize, pageNumber, hideDeleted);

        if (pageSize < 1 || pageNumber < 1) {
            log.warn("getAllEmployeesByFilterAndPagination: Invalid page size or page number. PageSize: {}, PageNumber: {}", pageSize, pageNumber);
            throw new InvalidInputException("Page size and page number must be at least 1");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "deleted").and(Sort.by(Sort.Direction.ASC, "username"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<Employee> employeePage = hideDeleted
                ? employeeRepository.findAllByDeletedFalse(pageable)
                : employeeRepository.findAll(pageable);

        Page<EmployeeDto> employeeDtoPage = employeePage.map(employee -> modelMapper.map(employee, EmployeeDto.class));
        log.info("getAllEmployeesByFilterAndPagination: Found {} employees", employeeDtoPage.getTotalElements());

        log.trace("getAllEmployeesByFilterAndPagination method ends. PageSize: {}, PageNumber: {}, HideDeleted: {}", pageSize, pageNumber, hideDeleted);
        return employeeDtoPage;
    }
}
