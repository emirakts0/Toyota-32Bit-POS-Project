package com.userservice.dto;

import lombok.Data;

import java.util.Set;
@Data
public class EmployeeDto {
    private Long id;
    private String username;
    private String email;
    private Set<RoleDto> roles;
    private boolean deleted;
}
