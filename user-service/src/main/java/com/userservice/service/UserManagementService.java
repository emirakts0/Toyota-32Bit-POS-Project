package com.userservice.service;

import com.userservice.dto.RegisterRequestDto;

import java.util.Set;


public interface UserManagementService {

    Set<RegisterRequestDto> registerEmployee(Set<RegisterRequestDto> registerRequestDtoSet);

}
