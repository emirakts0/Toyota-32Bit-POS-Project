package com.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;


@Data
public class UpdateRequestDto {

    @Size(min = 1, max = 50, message = "Username length must be between 1 and 50 characters.")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain spaces")
    private String username;

    @Email(message = "Invalid email format.")
    private String email;

    @Size(min = 8, max = 100, message = "Password length must be between 8 and 100 characters.")
    @Pattern(regexp = "\\S+", message = "Password cannot be blank or contain only whitespace")
    private String password;

    @Size(min = 1, message = "At least one role is required.")
    private Set< @Valid RoleDto> roles;

    @Size(min = 1, max = 50, message = "Name length must be less than or equal to 50 characters.")
    @Pattern(regexp = "^[^\\s].*[^\\s]$", message = "Name cannot start or end with a space")
    private String name;

    @Size(min = 1, max = 50, message = "Surname length must be less than or equal to 50 characters.")
    @Pattern(regexp = "^[^\\s].*[^\\s]$", message = "Surname cannot start or end with a space")
    private String surname;
}
