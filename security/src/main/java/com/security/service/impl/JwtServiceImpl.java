package com.security.service.impl;

import com.security.exception.InvalidTokenException;
import com.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.key}")
    String JWT_KEY;
    long fourHoursInMillis = 4 * 60 * 60 * 1000;

    @Override
    public String generateToken(String username) {
        log.trace("generateToken method begins. Username: {}", username);

        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(now.getTime() + fourHoursInMillis);

        String token = Jwts.builder()
                .setIssuer("POS Security")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setSubject(username)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("generateToken: Token generated successfully for username: {}", username);
        log.trace("generateToken method ends. Username: {}", username);
        return token;
    }


    @Override
    public boolean validateToken(String token) {
        log.trace("validateToken method begins. TOKEN");

        try {
            if(token.trim().isEmpty()) {
                log.warn("validateToken: Token is empty");
                return false;
            }

            Date expirationDate = extractClaim(token, Claims::getExpiration);
            String username = extractClaim(token, Claims::getSubject);

            if (username == null || username.trim().isEmpty()) {
                log.warn("validateToken: Username extracted from token is null or empty");
                return false;
            }

            boolean isValid = !expirationDate.before(new Date());
            log.info("validateToken: Token validation result: {}", isValid);

            log.trace("validateToken method ends. TOKEN");
            return isValid;

        } catch (Exception e) {
            log.error("validateToken: Exception occurred while validating token", e);
            return false;
        }
    }

    @Override
    public String getUsername(String token) {
        log.trace("getUsername method begins. TOKEN");

        String username = extractClaim(token, Claims::getSubject);
        log.info("getUsername: Username extracted from token: {}", username);

        log.trace("getUsername method ends. TOKEN");
        return username;
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimExtractor) {
        log.trace("extractClaim method begins. TOKEN");

        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        T claim = claimExtractor.apply(claims);
        log.debug("extractClaim: Extracted claim from token: TOKEN");

        log.trace("extractClaim method ends. TOKEN");
        return claim;
    }


    private SecretKey getSigningKey() {
        log.trace("getSigningKey method begins.");

        byte[] keyBytes = Decoders.BASE64.decode(JWT_KEY);
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        log.trace("getSigningKey method ends.");
        return signingKey;
    }
}
