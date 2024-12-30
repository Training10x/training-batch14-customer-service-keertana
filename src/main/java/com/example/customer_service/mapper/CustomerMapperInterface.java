package com.example.customer_service.mapper;
import org.mapstruct.Mapper;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapperInterface {

    Customer toEntity(CustomerDTO customerDTO);
    CustomerDTO toDto(Customer customer);
}
