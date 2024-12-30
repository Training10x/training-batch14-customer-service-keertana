package com.example.customer_service.mapper;
import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerServiceMapperInterfaceTest {

    private final CustomerMapperInterface mapper = Mappers.getMapper(CustomerMapperInterface.class);

    @Test
     void testEntityToDtoMapping() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setIndustry("IT");
        customer.setCompanySize("50");
        customer.setEmail("johndoe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setAddress("123 Main St");
        customer.setStatus("enabled");

        CustomerDTO dto = mapper.toDto(customer);

        // Assertions
        assertEquals(customer.getId(), dto.getId());
        assertEquals(customer.getName(), dto.getName());
        assertEquals(customer.getIndustry(), dto.getIndustry());
        assertEquals(customer.getCompanySize(), dto.getCompanySize());
        assertEquals(customer.getEmail(), dto.getEmail());
        assertEquals(customer.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(customer.getAddress(), dto.getAddress());
        assertEquals(customer.getStatus(), dto.getStatus());
    }

    @Test
     void testDtoToEntityMapping() {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setIndustry("IT");
        dto.setCompanySize("50");
        dto.setEmail("johndoe@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setAddress("123 Main St");
        dto.setStatus("enabled");

        Customer customer = mapper.toEntity(dto);

        // Assertions
        assertEquals(dto.getId(), customer.getId());
        assertEquals(dto.getName(), customer.getName());
        assertEquals(dto.getIndustry(), customer.getIndustry());
        assertEquals(dto.getCompanySize(), customer.getCompanySize());
        assertEquals(dto.getEmail(), customer.getEmail());
        assertEquals(dto.getPhoneNumber(), customer.getPhoneNumber());
        assertEquals(dto.getAddress(), customer.getAddress());
        assertEquals(dto.getStatus(), customer.getStatus());
    }
}