package com.security.auth;

import com.security.exception.BadCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Custom authentication provider for user authentication using username and password.
 * This class integrates with Spring Security and provides custom authentication logic.
 * @author Emir Aktaş
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;


    /**
     * Authenticates a user based on the provided authentication object.
     *
     * @param authentication the authentication request object containing the username and password
     * @return an authenticated UsernamePasswordAuthenticationToken if the authentication is successful
     * @throws AuthenticationException if the authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.trace("Attempting to authenticate user: {}", authentication.getName());

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        log.debug("authenticate: Loaded user details for user: {}", username);

        if(passwordEncoder.matches(password, userDetails.getPassword())) {
            log.info("authenticate: Authentication successful for user: {}", username);

            return new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    userDetails.getAuthorities());
        } else {
            log.warn("authenticate: Authentication failed for user: {} - Invalid password", username);
            throw new BadCredentialsException("Invalid password");
        }
    }


    /**
     * Checks if this authentication provider supports the specified authentication type.
     *
     * @param authentication the class of the authentication object
     * @return true if the authentication type is supported, false otherwise
     */
    @Override
    public boolean supports(Class<?> authentication) {
        boolean isSupported = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        log.trace("supports: Checking if authentication type is supported: {}", isSupported);
        return isSupported;
    }
}
