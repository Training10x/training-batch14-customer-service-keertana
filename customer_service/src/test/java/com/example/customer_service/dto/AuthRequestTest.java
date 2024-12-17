package com.example.customer_service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {

    @Test
    void testSetAndGetValues() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("password123");

        assertEquals("user@example.com", authRequest.getEmail());
        assertEquals("password123", authRequest.getPassword());
    }

    @Test
    void testValidInputs() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("password123");

        assertTrue(isValidEmail(authRequest.getEmail()), "The email should be valid");
        assertFalse(authRequest.getPassword().isBlank(), "The password should not be empty");
    }

    @Test
    void testInvalidEmailFormat() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("invalid-email");
        authRequest.setPassword("password123");

        assertFalse(isValidEmail(authRequest.getEmail()), "The email should be invalid");
    }

    @Test
    void testEmailIsEmpty() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("");
        authRequest.setPassword("password123");

        assertTrue(authRequest.getEmail().isBlank(), "The email should be empty");
    }

    @Test
    void testPasswordIsEmpty() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("");

        assertTrue(authRequest.getPassword().isBlank(), "The password should be empty");
    }

    @Test
    void testNullValues() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail(null);
        authRequest.setPassword(null);

        assertNull(authRequest.getEmail(), "The email should be null");
        assertNull(authRequest.getPassword(), "The password should be null");
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
