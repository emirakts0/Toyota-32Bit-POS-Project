package com.userservice.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleDto {

    @Pattern(regexp = "ROLE_CASHIER|ROLE_ADMIN|ROLE_MANAGER",
             message = "Invalid role name. Valid roles are: ROLE_CASHIER, ROLE_ADMIN, ROLE_MANAGER")
    private String roleName;
}
