package com.example.customer_service.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchCriteria {
    private Long id;
    private String name;
    private String industry;
    private String companySize;
    private String email;
    private String phoneNumber;
    private String address;
    private String status;
    @Min(value = 0, message = "Page number cannot be negative")
    private int page = 0; // Default value
    @Min(value = 1, message = "Limit must be at least 1")
    private int limit = 10; // Default value
}
