package com.userservice.controller;

import com.userservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("employee")
public class UserManagementController {

    private final UserManagementService userManagementService;


    @PostMapping
    public ResponseEntity<String> registerEmployeeById(){

        return null;
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployeeById(){
        return null;
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable Long id){

        return null;
    }


    @PutMapping("reactivate-by-id/{id}")
    public ResponseEntity<String> reactivateEmployeeById(@PathVariable Long id){

        return null;
    }

}
