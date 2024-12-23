package com.example.customer_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerServiceDuplicateResourceExceptionTest {

    private String errorMessage;

    @BeforeEach
    void setUp() {
        errorMessage = "Customer already exists";
    }

    @Test
    void shouldCreateExceptionWithErrorMessage() {
        // Arrange & Act
        DuplicateResourceException exception =
                new DuplicateResourceException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());

        System.out.println("Test Passed: Exception created successfully.");
    }
}
