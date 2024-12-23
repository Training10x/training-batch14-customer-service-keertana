package com.example.customer_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerServiceResourceNotFoundExceptionTest {

    private String errorMessage;

    @BeforeEach
    void setUp() {
        errorMessage = "Customer not found";
    }

    @Test
    void shouldCreateExceptionWithErrorMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());

        System.out.println("Test Passed: Exception created successfully.");
    }
}
