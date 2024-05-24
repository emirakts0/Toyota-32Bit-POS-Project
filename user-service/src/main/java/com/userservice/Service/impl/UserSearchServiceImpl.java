package com.userservice.Service.impl;

import com.userservice.Service.UserSearchService;
import com.userservice.dto.EmployeeDto;
import com.userservice.exception.EmployeeNotFoundException;
import com.userservice.exception.InvalidInputException;
import com.userservice.model.Employee;
import com.userservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;


    @Override
    public EmployeeDto searchEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with username %s not found", username)));

        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Employee with username %d not found", id)));

        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    public Page<EmployeeDto> getAllEmployeesByFilterAndPagination(int pageSize, int pageNumber, boolean hideDeleted) {

        if (pageSize < 1 || pageNumber < 1)
            throw new InvalidInputException("page size and page number must be at least 1");

        Sort sort = Sort.by(Sort.Direction.DESC, "deleted").and(Sort.by(Sort.Direction.ASC, "username"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Page<Employee> employeePage = hideDeleted
                ? employeeRepository.findAllByDeletedFalse(pageable)
                : employeeRepository.findAll(pageable);

        return employeePage.map(employee -> modelMapper.map(employee, EmployeeDto.class));
    }
}
