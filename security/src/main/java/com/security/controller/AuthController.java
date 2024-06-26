package com.security.controller;

import com.security.dto.JwtResponse;
import com.security.dto.LoginRequestDto;
import com.security.service.LoginService;
import com.security.service.impl.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final CustomUserDetailsService customUserDetailsService;


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        log.trace("authenticateUser: Login request received for username: {}", loginRequest.getUsername());

        JwtResponse jwtResponse = JwtResponse.builder()
                .username(loginRequest.getUsername())
                .token(loginService.login(loginRequest)).build();

        log.info("authenticateUser: User authenticated successfully for username: {}", loginRequest.getUsername());
        return ResponseEntity.ok(jwtResponse);
    }


    @PostMapping("/user")
    public ResponseEntity<Boolean> userManagement(@RequestHeader("Authorization") String authorizationHeader) {
        log.trace("userManagement: Request received with Authorization header: TOKEN");
        return ResponseEntity.ok(true);
    }


    @PostMapping("/report/status/receipt")
    public ResponseEntity<Boolean> reportStatus(@RequestHeader("Authorization") String authorizationHeader) {
        log.trace("reportStatus: Request received with Authorization header: TOKEN");
        return ResponseEntity.ok(true);
    }


    @PostMapping("/report")
    public ResponseEntity<Boolean> report(@RequestHeader("Authorization") String authorizationHeader) {
        log.trace("report: Request received with Authorization header: TOKEN");
        return ResponseEntity.ok(true);
    }


    @PostMapping("/sale")
    public ResponseEntity<String> sale(@RequestHeader("Authorization") String authorizationHeader) {
        log.trace("sale: Request received with Authorization header: TOKEN");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = customUserDetailsService.getNameByUsername(authentication.getName());

        log.info("sale: Sale request processed for username: {}", authentication.getName());
        return ResponseEntity.ok("name-" + name);
    }


    @PostMapping("/product")
    public ResponseEntity<Boolean> product(@RequestHeader("Authorization") String authorizationHeader) {
        log.trace("product: Request received with Authorization header: TOKEN");
        return ResponseEntity.ok(true);
    }
}
