package com.example.customer_service.service.impl;
import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.exception.DuplicateResourceException;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.CustomerService;
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

@Service
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository customerRepository;
    private CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

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
    public boolean isEmailDuplicate(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean isPhoneDuplicate(String phoneNumber) {
            return customerRepository.findByPhoneNumber(phoneNumber).isPresent();
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
                    if (!customer.getEmail().equals(newEmail) &&
                            customerRepository.findByEmail(newEmail).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
                    }
                    customer.setEmail(newEmail);
                    break;

                case "phoneNumber":
                    String newPhoneNumber = (String) value;
                    if (!customer.getPhoneNumber().equals(newPhoneNumber) &&
                            customerRepository.findByPhoneNumber(newPhoneNumber).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already in use"); //409
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

    public Map<String, Object> searchCustomers(SearchCriteria criteria) {
        try {
            Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getLimit());
            Page<Customer> customerPage = customerRepository.findCustomersByFilters(criteria, pageable
            );

            List<CustomerDTO> customerDTOs = customerPage.getContent()
                    .stream()
                    .map(customerMapper::toDto)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("total_count", customerPage.getTotalElements());
            response.put("page_count", customerPage.getTotalElements() == 0 ? 0 : customerPage.getTotalPages());
            response.put("current_page", criteria.getPage());
            response.put("results", customerDTOs);

            return response;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search parameters", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search customers", e);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return "CREATE_CUSTOMER".equals(permission) || "SEARCH_CUSTOMER".equals(permission) ||
                "UPDATE_CUSTOMER_STATUS".equals(permission) || "UPDATE_CUSTOMER".equals(permission);
    }

    @Override
    public List<Customer> findAllCustomers() {
      return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

}
