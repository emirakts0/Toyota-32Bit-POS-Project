package com.userservice.controller;

import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.UpdateRequestDto;
import com.userservice.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

}
