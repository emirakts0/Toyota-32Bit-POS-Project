package com.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom handler for access denied exceptions in Spring Security.
 * This handler is triggered when a user tries to access a resource they do not have permission to access.
 * @author Emir Aktaş
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;


    /**
     * Handles access denied exceptions by sending a JSON response with an appropriate error message and HTTP status.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param accessDeniedException the access denied exception
     * @throws IOException if an I/O error occurs during handling
     * @throws ServletException if a servlet error occurs during handling
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.trace("handle: Access denied handler triggered for request: {}", request.getRequestURI());

        String requiredRole = getRequiredRole(request);
        log.debug("handle: Required role for this request: {}", requiredRole);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied: You need the role " + requiredRole + " to access this resource"
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        log.error("handle: Access denied for request: {} with message: {}",
                request.getRequestURI(), accessDeniedException.getMessage());
    }


    /**
     * Determines the required role for accessing a given request URI.
     *
     * @param request the HTTP request
     * @return the required role as a string
     */
    private String getRequiredRole(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/auth/user-management")) {
            return "ADMIN";
        } else if (requestURI.startsWith("/auth/report/status/receipt")) {
            return "MANAGER or CASHIER";
        } else if (requestURI.startsWith("/auth/report")) {
            return "MANAGER";
        } else if (requestURI.startsWith("/auth/sale")) {
            return "CASHIER";
        }
        return "UNKNOWN";
    }
}
