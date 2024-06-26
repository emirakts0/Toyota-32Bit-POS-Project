package com.security.service;

/**
 * Service interface for handling JWT (JSON Web Token) operations.
 * @author Emir Aktaş
 */
public interface JwtService {

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username for which the token is to be generated
     * @return the generated JWT token
     */
    String generateToken(String username);


    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);


    /**
     * Extracts the username from the given JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the username extracted from the token
     */
    String getUsername(String token);
}
