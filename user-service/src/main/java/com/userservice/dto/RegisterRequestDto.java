package com.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;


@Data
public class RegisterRequestDto {

    @NotBlank(message = "Username cannot be empty or null.")
    @Size(min = 1, max = 50, message = "Username length must be between 1 and 50 characters.")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain spaces")
    private String username;

    @NotBlank(message = "Email cannot be empty or null.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotNull(message = "At least one role is required.")
    @Size(min = 1, message = "At least one role is required.")
    private Set< @Valid RoleDto> roles;

    @NotBlank(message = "password cannot null or empty")
    @Size(min = 8, max = 100, message = "Password length must be between 8 and 100 characters.")
    private String password;

    @NotBlank(message = "name cannot be empty or null.")
    @Size(min = 1, max = 50, message = "Name length must be less than or equal to 50 characters.")
    @Pattern(regexp = "^[^\\s].*[^\\s]$", message = "Name cannot start or end with a space")
    private String name;

    @NotBlank(message = "surname cannot be empty or null.")
    @Size(min = 1, max = 50, message = "Surname length must be less than or equal to 50 characters.")
    @Pattern(regexp = "^[^\\s].*[^\\s]$", message = "Surname cannot start or end with a space")
    private String surname;
}
