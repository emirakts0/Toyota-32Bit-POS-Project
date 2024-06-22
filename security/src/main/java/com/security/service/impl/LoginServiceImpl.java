package com.security.service.impl;

import com.security.dto.LoginRequestDto;
import com.security.exception.BadCredentialsException;
import com.security.service.JwtService;
import com.security.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements LoginService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public String login(LoginRequestDto loginRequestDto) {
        log.trace("login method begins. Username: {}",loginRequestDto.getUsername());

        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        try {
            authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(username, password) );

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(userDetails.getUsername());

            log.trace("login method ends. Username: {}", username);
            return token;

        } catch (AuthenticationException e) {
            log.warn("login: Authentication failed for username: {}", username);
            throw new BadCredentialsException("Bad credentials");
        }
    }
}
