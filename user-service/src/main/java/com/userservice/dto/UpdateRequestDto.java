package com.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;


@Data
public class UpdateRequestDto {

    @Size(min = 1, max = 50, message = "Username length must be between 1 and 50 characters.")
    @Pattern(regexp = "\\S+", message = "Username cannot be blank or contain only whitespace")
    private String username;

    @Email(message = "Invalid email format.")
    private String email;

    @Size(min = 8, max = 100, message = "Password length must be between 8 and 100 characters.")
    @Pattern(regexp = "\\S+", message = "Password cannot be blank or contain only whitespace")
    private String password;

    @Size(min = 1, message = "At least one role is required.")
    private Set< @Valid RoleDto> roles;
}
