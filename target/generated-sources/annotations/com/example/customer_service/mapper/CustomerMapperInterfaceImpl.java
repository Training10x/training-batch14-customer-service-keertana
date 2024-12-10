package com.example.customer_service.mapper;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-10T00:00:53-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class CustomerMapperInterfaceImpl implements CustomerMapperInterface {

    @Override
    public Customer toEntity(CustomerDTO customerDTO) {
        if ( customerDTO == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setId( customerDTO.getId() );
        customer.setName( customerDTO.getName() );
        customer.setIndustry( customerDTO.getIndustry() );
        customer.setCompanySize( customerDTO.getCompanySize() );
        customer.setEmail( customerDTO.getEmail() );
        customer.setPhoneNumber( customerDTO.getPhoneNumber() );
        customer.setAddress( customerDTO.getAddress() );
        customer.setStatus( customerDTO.getStatus() );

        return customer;
    }

    @Override
    public CustomerDTO toDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerDTO customerDTO = new CustomerDTO();

        customerDTO.setId( customer.getId() );
        customerDTO.setName( customer.getName() );
        customerDTO.setIndustry( customer.getIndustry() );
        customerDTO.setCompanySize( customer.getCompanySize() );
        customerDTO.setEmail( customer.getEmail() );
        customerDTO.setPhoneNumber( customer.getPhoneNumber() );
        customerDTO.setAddress( customer.getAddress() );
        customerDTO.setStatus( customer.getStatus() );

        return customerDTO;
    }
}