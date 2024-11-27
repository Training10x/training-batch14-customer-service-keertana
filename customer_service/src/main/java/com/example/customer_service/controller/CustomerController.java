package com.example.customer_service.controller;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
@RestController
@RequestMapping("api/v1/customers")

public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        if(!customerService.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized access", "status", 401));
        }
        if (!customerService.hasPermission("CREATE_CUSTOMER")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden", "status", 403));
        }

        if (customerDTO.getId() != null && !customerRepository.findById(customerDTO.getId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Parent entity not found", "status", 404));
        }
        // Validate Email
        if (customerDTO.getEmail() == null || customerDTO.getEmail().trim().isEmpty() || !customerDTO.getEmail().contains("@")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid email format", "status", 400));
        }

        // Validate Phone Number
        if (customerDTO.getPhoneNumber() == null || customerDTO.getPhoneNumber().trim().isEmpty() ||!customerDTO.getPhoneNumber().matches("\\d{10}")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Phone number is not valid", "status", 400));
        }

        // Check for duplicate email or phone number
        if (customerService.isEmailOrPhoneDuplicate(customerDTO.getEmail(), customerDTO.getPhoneNumber())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "Email or phone number already exists",
                            "status", 409
                    ));
        }

        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String companySize,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized access", "status", 401));
        }

        // Check if the user has permission to perform the search
        if (!customerService.hasPermission("SEARCH_CUSTOMER")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden", "status", 403));
        }
        if (page < 0 || limit <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid pagination parameters", "status", 400));
        }
        try{
            Map<String, Object> result = customerService.searchCustomers(name, industry, companySize, email, phoneNumber, address, status, page, limit);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "status", 400));
        } catch(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to search customers", "status", 500));
        }

    }
    @PatchMapping("/{customer_id}")
    public ResponseEntity<?> updateCustomerStatus(@PathVariable Long customer_id, @RequestBody Map<String, Object> updates) {
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized access", "status", 401));
        }

        // Check if the user has permission to update customer status
        if (!customerService.hasPermission("UPDATE_CUSTOMER_STATUS")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden", "status", 403));
        }
        if(!updates.containsKey("status")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Status field is required", "status", 400));
        }

        String status = updates.get("status").toString();
        if(!status.equals("enabled") && !status.equals("disabled")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid status value", "status", 400));
        }
        if(!customerService.customerExists(customer_id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Customer not found", "status", 404));
        }

        boolean updatedStatus = customerService.updateCustomerStatus(customer_id, status);
        if (updatedStatus) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("message", "Customer status updated successfully", "status", 200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to update customer status", "status", 500));
        }
    }
    @PutMapping("/{customer_id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long customer_id,@RequestBody Map<String, Object> updates){
        // Check if the user is authenticated
        if (!customerService.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized access", "status", 401));
        }

        // Check if the user has permission to update customers
        if (!customerService.hasPermission("UPDATE_CUSTOMER")) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Forbidden", "status", 403));
        }
        // If the customer is not found, return 404
        if (!customerService.customerExists(customer_id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Customer not found", "status", 404));
        }

        // Validate Email
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid email format", "status", 400));
            }
        }

        // Validate Phone Number
        if (updates.containsKey("phoneNumber")) {
            String phoneNumber = (String) updates.get("phoneNumber");
            if (phoneNumber == null || phoneNumber.trim().isEmpty() || !phoneNumber.matches("\\d{10}")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid phone number format", "status", 400));
            }
        }

        CustomerDTO updatedCustomer = customerService.updateCustomer(customer_id, updates);
        // If customer exists, return 200 OK
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }
}
