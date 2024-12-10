package com.example.customer_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.context.annotation.Import;

import javax.naming.ServiceUnavailableException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(GlobalExceptionHandler.class)
class CustomerServiceGlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup()
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleRuntimeException_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test-runtime-exception")
                        .requestAttr("javax.servlet.error.exception", new RuntimeException("This is a runtime exception")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("This is a runtime exception"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void handleServiceUnavailableException_shouldReturnServiceUnavailable() throws Exception {
        mockMvc.perform(get("/test-service-unavailable-exception")
                        .requestAttr("javax.servlet.error.exception", new ServiceUnavailableException("Service is unavailable")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Service is unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void handleDuplicateResourceException_shouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test-duplicate-resource-exception")
                        .requestAttr("javax.servlet.error.exception", new DuplicateResourceException("Resource already exists")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Resource already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void handleIllegalArgumentException_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test-illegal-argument-exception")
                        .requestAttr("javax.servlet.error.exception", new IllegalArgumentException("Invalid argument")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid argument"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test-generic-exception")
                        .requestAttr("javax.servlet.error.exception", new Exception("Generic exception occurred")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").value("Generic exception occurred"))
                .andExpect(jsonPath("$.status").value(500));
    }
}
