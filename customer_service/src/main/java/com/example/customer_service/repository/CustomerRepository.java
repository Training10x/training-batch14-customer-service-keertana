package com.example.customer_service.repository;

import com.example.customer_service.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org. springframework.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email); // this method is to find customer by email
    Optional<Customer> findByPhoneNumber(String phoneNumber); // this method is to find customer by phone number

    @Query("SELECT c FROM Customer c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:industry IS NULL OR c.industry = :industry) AND " +
            "(:companySize IS NULL OR c.companySize = :companySize) AND " +
            "(:email IS NULL OR c.email LIKE %:email%) AND " +
            "(:phoneNumber IS NULL OR c.phoneNumber = :phoneNumber) AND " +
            "(:address IS NULL OR c.address LIKE %:address%) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<Customer> findCustomersByFilters(@Param("name") String name,
                                          @Param("industry") String industry,
                                          @Param("companySize") String companySize,
                                          @Param("email") String email,
                                          @Param("phoneNumber") String phoneNumber,
                                          @Param("address") String address,
                                          @Param("status") String status,
                                          Pageable pageable);

}
