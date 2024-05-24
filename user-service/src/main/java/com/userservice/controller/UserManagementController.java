package com.userservice.controller;

import com.userservice.Service.UserManagementService;
import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.UpdateRequestDto;
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
@RequestMapping("/user/management")
public class UserManagementController {

    private final UserManagementService userManagementService;


    @PostMapping
    public ResponseEntity<String> registerEmployee(@RequestBody Set<@Valid RegisterRequestDto> registerRequestDtos){

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

}
