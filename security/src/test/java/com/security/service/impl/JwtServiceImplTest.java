package com.security.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class JwtServiceImplTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "JWT_KEY", "1111111111111111111111111111111111111111111111111111111111111111");
    }

    @Test
    void whenGenerateToken_thenTokenShouldBeValid() {
        String username = "testUser";
        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertTrue(jwtService.validateToken(token));
        assertEquals(username, jwtService.getUsername(token));
    }


    @Test
    void whenValidateToken_thenTokenShouldBeInvalid() {
        String invalidToken = "invalid.token.abc";

        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void whenValidateExpiredToken_thenTokenShouldBeInvalid() {
        String username = "testUser";

        ReflectionTestUtils.setField(jwtService, "fourHoursInMillis", 0L);
        String token = jwtService.generateToken(username);

        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void whenTokenIsEmpty_thenValidateTokenReturnsFalse() {
        assertFalse(jwtService.validateToken(""));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void whenUsernameIsNullOrEmpty_thenValidateTokenReturnsFalse(String subject) {
        String token = generateTokenWithSubject(subject);
        assertFalse(jwtService.validateToken(token));
    }

    private String generateTokenWithSubject(String subject) {
        Date now = new Date();
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("1111111111111111111111111111111111111111111111111111111111111111"));
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 100000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}