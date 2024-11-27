package com.example.customer_service.dto;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.Setter;

@Data
public class CustomerDTO{
    @Setter
    private Long id;

    @NotBlank(message = "Name field should not be empty")
    private String name;

    @NotBlank(message = "Industry field should not be empty")
    private String industry;

    @NotBlank(message = "Company Size field should not be empty")
    private String companySize;

    @Email(message = "Email format is not valid")
    @NotBlank(message = "Email field should not be empty")
    private String email;

    @NotBlank(message = "Phone Number should not be empty")
    private String phoneNumber;

    @NotBlank(message = "Address field should not be empty")
    private String address;

    @NotBlank(message = "Status field should not be empty")
    private String status;
}
