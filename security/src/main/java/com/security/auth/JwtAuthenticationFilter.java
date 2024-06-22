package com.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.exception.CustomAuthenticationEntryPoint;
import com.security.exception.InvalidTokenException;
import com.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT Authentication filter that processes and validates JWT tokens for incoming requests.
 * This filter intercepts requests, extracts the JWT token from the Authorization header,
 * validates it, and sets the authentication in the security context if the token is valid.
 * @author Emir Aktaş
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;


    /**
     * Filters incoming requests and processes JWT token authentication.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.trace("doFilterInternal: Processing request for URL: {}", request.getServletPath());

        if ("/auth/login".equals(request.getServletPath())) {
            filterChain.doFilter(request, response);
            log.debug("doFilterInternal: Skipping filter for login endpoint");
            return;
        }

        final String header = request.getHeader("Authorization");
        log.debug("doFilterInternal: Authorization Header: {}", header);

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("doFilterInternal: Authorization header is missing or invalid");
            handleInvalidTokenException(request, response,
                                        new InvalidTokenException("Authorization header is invalid"));
            return;
        }

        final String jwtToken = header.substring(7);
        log.debug("doFilterInternal: Extracted JWT Token: TOKEN");


        try {
            if (!jwtToken.isEmpty()
                    && jwtService.validateToken(jwtToken)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {


                String username = jwtService.getUsername(jwtToken);
                log.debug("doFilterInternal: Username extracted from JWT Token: '{}'", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.info("doFilterInternal: JWT Authentication successful for username: '{}'", username);
            } else {
                log.warn("doFilterInternal: Invalid token or authentication already exists");
                throw new InvalidTokenException("Invalid token received");
            }

        } catch (Exception e) {
            handleInvalidTokenException(request, response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }


    /**
     * Handles invalid token exceptions by logging the error and responding with an appropriate
     * error message using the {@link CustomAuthenticationEntryPoint}.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param e the exception that occurred
     * @throws IOException if an I/O error occurs during handling
     * @throws ServletException if an error occurs during handling
     */
    private void handleInvalidTokenException(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Exception e) throws IOException, ServletException {
        log.error("handleInvalidTokenException: Handling invalid token exception", e);

        AuthenticationException authException = new AuthenticationException(e.getMessage(), e) {};
        CustomAuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper);
        authenticationEntryPoint.commence(request, response, authException);
    }
}
