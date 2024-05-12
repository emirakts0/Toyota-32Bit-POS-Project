package com.userservice.service;

import com.userservice.dto.RegisterRequestDto;
import com.userservice.dto.UpdateRequestDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


public interface UserManagementService {

    Set<RegisterRequestDto> registerEmployee(Set<RegisterRequestDto> registerRequestDtoSet);

    @Transactional
    UpdateRequestDto updateEmployeeById(Long id, UpdateRequestDto updateRequestDto);

    @Transactional
    Long deleteEmployeeById(Long id);

    @Transactional
    Long reactivateEmployeeById(Long id);
}
