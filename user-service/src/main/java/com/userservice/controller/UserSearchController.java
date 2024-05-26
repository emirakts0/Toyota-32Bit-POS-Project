package com.userservice.controller;

import com.userservice.Service.UserSearchService;
import com.userservice.dto.EmployeeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/search")
public class UserSearchController {

    private final UserSearchService userSearchService;


    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(@RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(defaultValue = "1") int pageNumber,
                                                             @RequestParam(defaultValue = "true") boolean hideDeleted){
        log.trace("getAllEmployees endpoint called with pageSize: {}, pageNumber: {}, hideDeleted: {}", pageSize, pageNumber, hideDeleted);

        Page<EmployeeDto> employeeDtoPage = userSearchService.getAllEmployeesByFilterAndPagination(
                pageSize,
                pageNumber,
                hideDeleted);
        return ResponseEntity.ok(employeeDtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id){
        log.trace("getEmployeeById endpoint called for ID: {}", id);

        EmployeeDto employeeDto = userSearchService.getEmployeeById(id);
        return ResponseEntity.ok(employeeDto);
    }

    @GetMapping("username/{username}")
    public ResponseEntity<EmployeeDto> getEmployeeByUsername(@PathVariable String username){
        log.trace("getEmployeeByUsername endpoint called for username: {}", username);

        EmployeeDto employeeDto = userSearchService.searchEmployeeByUsername(username);
        return ResponseEntity.ok(employeeDto);
    }

}
