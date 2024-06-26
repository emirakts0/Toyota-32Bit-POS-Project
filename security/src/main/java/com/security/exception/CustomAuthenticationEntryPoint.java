package com.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Custom entry point for authentication exceptions in Spring Security.
 * This entry point is triggered when an unauthenticated user tries to access a secured resource.
 * @author Emir Aktaş
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;


    /**
     * Handles authentication exceptions by sending a JSON response with an appropriate error message and HTTP status.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the authentication exception
     * @throws IOException if an I/O error occurs during handling
     * @throws ServletException if a servlet error occurs during handling
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.trace("commence: Authentication entry point triggered for request: {}", request.getRequestURI());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), authException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        log.error("commence: Error while processing authentication entry point for request: {} with message: {}",
                request.getRequestURI(), authException.getMessage(), authException);
    }
}
