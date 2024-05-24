package com.userservice.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RoleDto {

    @Pattern(regexp = "CASHIER|ADMIN|MANAGER",
             message = "Invalid role name. Valid roles are: CASHIER, ADMIN, MANAGER")
    private String roleName;
}
