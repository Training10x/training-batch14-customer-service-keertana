package com.example.customer_service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testSetAndGetToken() {
        AuthResponse authResponse = new AuthResponse("sample-token");

        // Validate the constructor value
        assertEquals("sample-token", authResponse.getToken());

        // Modify the token and validate the updated value
        authResponse.setToken("new-token");
        assertEquals("new-token", authResponse.getToken());
    }

    @Test
    void testNullToken() {
        AuthResponse authResponse = new AuthResponse(null);

        // Validate that the token is null
        assertNull(authResponse.getToken(), "The token should be null");
    }

    @Test
    void testEmptyToken() {
        AuthResponse authResponse = new AuthResponse("");

        // Validate that the token is empty
        assertTrue(authResponse.getToken().isEmpty(), "The token should be empty");
    }
}
