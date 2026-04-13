package com.neobank.backend.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
        "3f8a2b1d9e4c7f0a6b3d8e2c5a9f1b4d7e0c3a6f9b2d5e8a1c4f7b0d3e6a9c20";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_containsCorrectClaims() {
        String token = jwtUtil.generateToken(1L, "test@neobank.in", "CUSTOMER");

        Claims claims = jwtUtil.extractClaims(token);

        assertEquals("test@neobank.in", claims.getSubject());
        assertEquals(1L, ((Number) claims.get("userId")).longValue());
        assertEquals("CUSTOMER", claims.get("role"));
    }

    @Test
    void generateToken_isValid() {
        String token = jwtUtil.generateToken(1L, "test@neobank.in", "CUSTOMER");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken(1L, "test@neobank.in", "CUSTOMER");
        assertEquals("test@neobank.in", jwtUtil.extractEmail(token));
    }

    @Test
    void isTokenValid_returnsFalse_forTamperedToken() {
        String token = jwtUtil.generateToken(1L, "test@neobank.in", "CUSTOMER");
        String tampered = token + "tampered";
        assertFalse(jwtUtil.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_returnsFalse_forExpiredToken() {
        // Create JwtUtil with 1ms expiration
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        String token = shortLived.generateToken(1L, "test@neobank.in", "CUSTOMER");

        // Wait for expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertFalse(shortLived.isTokenValid(token));
    }
}