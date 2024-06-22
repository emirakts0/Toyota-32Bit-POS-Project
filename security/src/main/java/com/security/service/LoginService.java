package com.security.service;

import com.security.dto.LoginRequestDto;
import com.security.exception.BadCredentialsException;

/**
 * Service interface for handling user login operations.
 * @author Emir Aktaş
 */
public interface LoginService {

    /**
     * Authenticates a user based on the provided login request and generates a JWT token.
     *
     * @param loginRequestDto the login request data transfer object containing the username and password
     * @return the generated JWT token
     * @throws BadCredentialsException if the authentication fails due to incorrect username or password
     */
    String login(LoginRequestDto loginRequestDto);
}
