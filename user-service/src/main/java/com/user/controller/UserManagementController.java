package com.user.controller;

import com.user.Service.UserManagementService;
import com.user.dto.RegisterRequestDto;
import com.user.dto.UpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RequiredArgsConstructor
@RestController
@Validated
@Slf4j
@RequestMapping("/user/management")
public class UserManagementController {

    private final UserManagementService userManagementService;


    @PostMapping
    public ResponseEntity<String> registerEmployee(@RequestBody Set<@Valid RegisterRequestDto> registerRequestDtos){
        log.trace("registerEmployee endpoint called with {} requests", registerRequestDtos.size());

        userManagementService.registerEmployee(registerRequestDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body("Employees saved successfully");
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateEmployeeById(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateRequestDto updateRequestDto){
        log.trace("updateEmployeeById endpoint called for ID: {}", id);

        userManagementService.updateEmployeeById(id, updateRequestDto);

        return ResponseEntity.ok().body(String.format("Id with %d updated", id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable Long id){
        log.trace("deleteEmployeeById endpoint called for ID: {}", id);

        userManagementService.deleteEmployeeById(id);
        return ResponseEntity.ok().body(String.format("Id with %d deleted", id));
    }


    @PutMapping("reactivate-by-id/{id}")
    public ResponseEntity<String> reactivateEmployeeById(@PathVariable Long id){
        log.trace("reactivateEmployeeById endpoint called for ID: {}", id);

        userManagementService.reactivateEmployeeById(id);
        return ResponseEntity.ok().body(String.format("Id with %d reactivated", id));
    }

}
