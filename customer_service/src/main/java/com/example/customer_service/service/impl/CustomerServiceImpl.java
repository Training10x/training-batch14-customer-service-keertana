package com.example.customer_service.service.impl;

import ch.qos.logback.classic.Logger;
import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.exception.DuplicateResourceException;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.CustomerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerRepository.findByEmail(customerDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("This Email ID already exists");
        }

        if (customerRepository.findByPhoneNumber(customerDTO.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        Customer customer = customerMapper.toEntity(customerDTO);
        Customer newCustomer = customerRepository.save(customer);
        return customerMapper.toDto(newCustomer);
    }

    @Override
    public boolean isEmailOrPhoneDuplicate(String email, String phoneNumber) {
        return customerRepository.findByEmail(email).isPresent() ||
                customerRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    @Override
    public CustomerDTO updateCustomer(Long customerId, Map<String, Object> updates) {
        // fetching customer by ID
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Apply updates to allowed fields
        updates.forEach((field, value) -> {
            switch (field) {
                case "name":
                    customer.setName((String) value);
                    break;

                case "email":
                    String newEmail = (String) value;
                    if (customerRepository.findByEmail(newEmail).isPresent() &&
                            !customer.getEmail().equals(newEmail)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                    }
                    customer.setEmail(newEmail);
                    break;

                case "phoneNumber":
                    String newPhoneNumber = (String) value;
                    if (customerRepository.findByPhoneNumber(newPhoneNumber).isPresent() &&
                            !customer.getPhoneNumber().equals(newPhoneNumber)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already in use");
                    }
                    customer.setPhoneNumber(newPhoneNumber);
                    break;

                case "industry":
                    customer.setIndustry((String) value);
                    break;

                case "companySize":
                    customer.setCompanySize((String) value);
                    break;

                case "address":
                    customer.setAddress((String) value);
                    break;

                default:
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid field: " + field);
            }
        });

        // Save updated customer
        Customer updatedCustomer = customerRepository.save(customer);

        // Convert to DTO and return
        return customerMapper.toDto(updatedCustomer);
    }

    @Override
    public boolean customerExists(Long customerId) {
        return customerRepository.findById(customerId).isPresent();
    }

    @Override
    public boolean updateCustomerStatus(Long customerId, String status) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return false; // Customer not found
        }

        Customer customer = customerOpt.get();
        customer.setStatus(status); // Set the new status
        customerRepository.save(customer); // Save the updated customer
        return true;
    }

    @Override
    public Map<String, Object> searchCustomers(String name, String industry, String companySize, String email,
                                               String phoneNumber, String address, String status, int page, int limit) {
        final Logger log = (Logger) LoggerFactory.getLogger(CustomerServiceImpl.class);
        log.info("Search Request: name={}, industry={}, companySize={}, email={}, phoneNumber={}, address={}, status={}, page={}, limit={}",
                name, industry, companySize, email, phoneNumber, address, status, page, limit);

        try {
            Pageable pageable = PageRequest.of(page, limit);
            log.info("Pageable: {}", pageable);

            Page<Customer> customerPage = customerRepository.findCustomersByFilters(
                    name, industry, companySize, email, phoneNumber, address, status, pageable);

            log.info("Query executed successfully. Total records: {}", customerPage.getTotalElements());

            List<CustomerDTO> customerDTOs = customerPage.getContent()
                    .stream()
                    .map(customerMapper::toDto)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total_count", customerPage.getTotalElements());
            response.put("page_count", customerPage.getTotalPages());
            response.put("current_page", page);
            response.put("results", customerDTOs);

            return response;
        } catch (IllegalArgumentException e) {
            log.error("Invalid search parameters", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search parameters");
        } catch (Exception e) {
            log.error("Error during searchCustomers execution", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search customers");
        }
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return "CREATE_CUSTOMER".equals(permission) || "SEARCH_CUSTOMER".equals(permission) || "UPDATE_CUSTOMER_STATUS".equals(permission) || "UPDATE_CUSTOMER".equals(permission);
    }

}
