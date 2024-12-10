package com.example.customer_service.mapper;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerServiceMapperTest {

    @InjectMocks
    private CustomerMapper customerMapper;

    @BeforeEach
    public void setup() {
        customerMapper = new CustomerMapper();
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("johndoe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setAddress("123 Main St");
        customer.setIndustry("IT");
        customer.setCompanySize("50");
        customer.setStatus("enabled");
        // Act
        CustomerDTO customerDTO = customerMapper.toDto(customer);
        // Assert
        assertNotNull(customerDTO);
        assertEquals(1L, customerDTO.getId());
        assertEquals("John Doe", customerDTO.getName());
        assertEquals("johndoe@example.com", customerDTO.getEmail());
        assertEquals("1234567890", customerDTO.getPhoneNumber());
        assertEquals("123 Main St", customerDTO.getAddress());
        assertEquals("IT", customerDTO.getIndustry());
        assertEquals("50", customerDTO.getCompanySize());
        assertEquals("enabled", customerDTO.getStatus());

        System.out.println("Test Passed: toDto method tested successfully.");
    }
    @Test
    void toEntity_ShouldMapDtoToEntity() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("John Doe");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setStatus("enabled");
        // Act
        Customer customer = customerMapper.toEntity(customerDTO);
        // Assert
        assertNotNull(customer);
        assertEquals(1L, customer.getId());
        assertEquals("John Doe", customer.getName());
        assertEquals("johndoe@example.com", customer.getEmail());
        assertEquals("1234567890", customer.getPhoneNumber());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals("IT", customer.getIndustry());
        assertEquals("50", customer.getCompanySize());
        assertEquals("enabled", customer.getStatus());

        System.out.println("Test Passed: toEntity method tested successfully.");
    }
}

