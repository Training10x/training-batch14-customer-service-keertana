package com.example.customer_service.dto;
import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Data
public class CustomerDTO{

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
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Address field should not be empty")
    private String address;

    @NotBlank(message = "Status field should not be empty")
    @Pattern(regexp = "enabled|disabled", message = "Status must be 'enabled' or 'disabled'")
    private String status;

    @NotBlank(message = "Role field should not be empty")
    @Pattern(regexp = "ADMIN|USER", message = "Role must be 'ADMIN' or 'USER'")
    private String role;

    @NotBlank(message = "Password field should not be empty")
    private String password;

}
