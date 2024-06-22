package com.security.service.impl;

import com.security.dto.LoginRequestDto;
import com.security.exception.BadCredentialsException;
import com.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private LoginServiceImpl loginService;


    @Test
    void whenLoginWithValidCredentials_thenTokenShouldBeReturned() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername("validUser");
        loginRequestDto.setPassword("validPassword");

        UserDetails userDetails = User.builder()
                .username("validUser")
                .password("validPassword")
                .authorities(new ArrayList<>())
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("validUser")).thenReturn(userDetails);
        when(jwtService.generateToken("validUser")).thenReturn("validToken");

        String token = loginService.login(loginRequestDto);

        assertNotNull(token);
        assertEquals("validToken", token);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("validUser");
        verify(jwtService, times(1)).generateToken("validUser");
    }

    @Test
    void whenLoginWithInvalidCredentials_thenThrowBadCredentialsException() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername("invalidUser");
        loginRequestDto.setPassword("invalidPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Bad credentials") {});

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> loginService.login(loginRequestDto));

        assertEquals("Bad credentials", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(anyString());
    }

}