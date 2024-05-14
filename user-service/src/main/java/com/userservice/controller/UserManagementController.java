package com.userservice.controller;

import com.userservice.dto.EmployeeDto;
import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.UpdateRequestDto;
import com.userservice.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Set;


@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("employee")
public class UserManagementController {

    private final UserManagementService userManagementService;


    @PostMapping
    public ResponseEntity<String> registerEmployeeById( @RequestBody Set<@Valid RegisterRequestDto> registerRequestDtos){

        userManagementService.registerEmployee(registerRequestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body("Employees saved successfully");
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployeeById(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateRequestDto updateRequestDto){
        userManagementService.updateEmployeeById(id, updateRequestDto);
        return ResponseEntity.ok().body(String.format("Id with %d updated", id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable Long id){

        userManagementService.deleteEmployeeById(id);
        return ResponseEntity.ok().body(String.format("Id with %d deleted", id));
    }


    @PutMapping("reactivate-by-id/{id}")
    public ResponseEntity<String> reactivateEmployeeById(@PathVariable Long id){

        userManagementService.reactivateEmployeeById(id);
        return ResponseEntity.ok().body(String.format("Id with %d reactivated", id));
    }


    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(@RequestParam(defaultValue = "10") int pageSize,
                                                             @RequestParam(defaultValue = "1") int pageNumber,
                                                             @RequestParam(defaultValue = "true") boolean hideDeleted){

        Page<EmployeeDto> employeeDtoPage = userManagementService.getAllUsersByFilterAndPagination(
                pageSize,
                pageNumber,
                hideDeleted);
        return ResponseEntity.ok(employeeDtoPage);
    }


    @GetMapping("/{username}")
    public ResponseEntity<EmployeeDto> getEmployeeByUsername(@PathVariable String username){

        EmployeeDto employeeDto = userManagementService.searchUserByUsername(username);
        return ResponseEntity.ok(employeeDto);
    }

}
