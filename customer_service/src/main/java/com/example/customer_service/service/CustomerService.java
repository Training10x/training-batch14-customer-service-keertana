package com.example.customer_service.service;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.repository.CustomerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO); // Create a new customer

    boolean isEmailOrPhoneDuplicate(@Email(message = "Email format is not valid") @NotBlank(message = "Email field should not be empty") String email, @NotBlank(message = "Phone Number should not be empty") String phoneNumber);

    CustomerDTO updateCustomer(Long customerId, Map<String, Object> updates);

    boolean customerExists(Long customerId);

    boolean updateCustomerStatus(Long customerId, String status);

    Map<String, Object> searchCustomers(String name, String industry, String companySize, String email, String phoneNumber, String address, String status, int page, int limit);

    boolean isAuthenticated();

    boolean hasPermission(String permission);
}
