package com.example.customer_service.controller;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("api/v1/customers")

public class CustomerController {
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String UNAUTHORIZED = "Unauthorized access";
    private static final String FORBIDDEN = "Forbidden";
    private static final String NOT_FOUND = "Customer not found";
    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";

    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    public ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of(MESSAGE, message, STATUS, status.value()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        if(!customerService.isAuthenticated()) {
            return buildResponse(HttpStatus.UNAUTHORIZED, UNAUTHORIZED); //401
        }
        if (!customerService.hasPermission("CREATE_CUSTOMER")) {
            return buildResponse(HttpStatus.FORBIDDEN, FORBIDDEN); //403
        }

        if (customerDTO.getId() != null && !customerRepository.findById(customerDTO.getId()).isPresent()) {
            return buildResponse(HttpStatus.NOT_FOUND, NOT_FOUND); //404
        }
        // Validate Email
        if (customerDTO.getEmail() == null || customerDTO.getEmail().trim().isEmpty() || !customerDTO.getEmail().contains("@")) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Invalid email format"); //400
        }

        // Validate Phone Number
        if (customerDTO.getPhoneNumber() == null || customerDTO.getPhoneNumber().trim().isEmpty()
                ||!customerDTO.getPhoneNumber().matches("\\d{10}")) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Invalid phone number format"); //400
        }

        // Check for duplicate email or phone number
        if (customerService.isEmailDuplicate(customerDTO.getEmail()) || customerService.isPhoneDuplicate(customerDTO.getPhoneNumber())) {
            return buildResponse(HttpStatus.CONFLICT, "Already exists");
        }


        customerService.createCustomer(customerDTO);
        return buildResponse(HttpStatus.CREATED, "Customer created successfully");
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getAllCustomers() {
        List<Customer> customers = customerService.findAllCustomers();
        return new ResponseEntity<>(Map.of("customers", customers), HttpStatus.OK);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        return new ResponseEntity<>(Map.of("customer", customer), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCustomers(@Valid SearchCriteria searchCriteria) {
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return buildResponse(HttpStatus.UNAUTHORIZED, UNAUTHORIZED); //401
        }


        // Check if the user has permission to perform the search
        if (!customerService.hasPermission("SEARCH_CUSTOMER")) {
            return buildResponse(HttpStatus.FORBIDDEN, FORBIDDEN); //403
        }
        if (searchCriteria.getPage() < 0 || searchCriteria.getLimit() <= 0) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Invalid search parameter"); //400
        }

        try{
            Map<String, Object> result = customerService.searchCustomers(searchCriteria);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Invalid search parameter"); //400
        } catch(Exception e) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search customers"); //500
        }

    }
    @PatchMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> updateCustomerStatus(@PathVariable Long customerId, @RequestBody Map<String, Object> updates) {
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return buildResponse(HttpStatus.UNAUTHORIZED, UNAUTHORIZED); //401
        }

        // Check if the user has permission to update customer status
        if (!customerService.hasPermission("UPDATE_CUSTOMER_STATUS")) {
            return buildResponse(HttpStatus.FORBIDDEN, FORBIDDEN); //403
        }
        if(!updates.containsKey(STATUS)) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Status field is required"); //400
        }

        String status = updates.get(STATUS).toString();
        if(!status.equals(ENABLED) && !status.equals(DISABLED)) {
            return buildResponse(HttpStatus.BAD_REQUEST, "Invalid status value"); //400
        }
        if(!customerService.customerExists(customerId)) {
            return buildResponse(HttpStatus.NOT_FOUND, NOT_FOUND); //404
        }

        boolean updatedStatus = customerService.updateCustomerStatus(customerId, status);
        if (updatedStatus) {
            return buildResponse(HttpStatus.OK, "Customer status updated successfully"); //200
        } else {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update customer status"); //500
        }
    }
    @PutMapping("/{customerId}")
    public ResponseEntity<Map<String, Object>> updateCustomer(@PathVariable Long customerId, @RequestBody Map<String, Object> updates){
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return buildResponse(HttpStatus.UNAUTHORIZED, UNAUTHORIZED); //401
        }

        // Check if the user has permission to update customers
        if (!customerService.hasPermission("UPDATE_CUSTOMER")) {
            return buildResponse(HttpStatus.FORBIDDEN, FORBIDDEN); //403
        }
        // If the customer is not found, return 404
        if (!customerService.customerExists(customerId)) {
            return buildResponse(HttpStatus.NOT_FOUND, NOT_FOUND); //404
        }

        // Validate Email
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid email format"); //400
            }
        }

        // Validate Phone Number
        if (updates.containsKey("phoneNumber")) {
            String phoneNumber = (String) updates.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.trim().isEmpty() || !phoneNumber.matches("\\d{10}")) {
                return buildResponse(HttpStatus.BAD_REQUEST, "Invalid phone number format"); //400
            }
        }

        customerService.updateCustomer(customerId, updates);
        // If customer exists, return 200 OK
        return buildResponse(HttpStatus.OK, "Customer updated successfully");
    }
}