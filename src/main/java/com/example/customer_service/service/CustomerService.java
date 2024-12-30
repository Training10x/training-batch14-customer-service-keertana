package com.example.customer_service.service;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


import java.util.List;
import java.util.Map;

public interface CustomerService {


    CustomerDTO createCustomer(CustomerDTO customerDTO);


    boolean isEmailDuplicate(@Email(message = "Invalid email format") String email);

    boolean isPhoneDuplicate(@NotBlank(message = "Phone Number should not be empty")
                             @NotBlank(message = "Phone Number should not be empty") String phoneNumber);

    CustomerDTO updateCustomer(Long customerId, Map<String, Object> updates);

    boolean customerExists(Long customerId);

    boolean updateCustomerStatus(Long customerId, String status);

    Map<String, Object> searchCustomers(SearchCriteria criteria);

    boolean isAuthenticated();

    boolean hasPermission(String permission);

    List<Customer> findAllCustomers();

    Customer getCustomerById(Long customerId);

    boolean isCustomerOwnedByEmail(Long customerId, String currentUserEmail);
}
