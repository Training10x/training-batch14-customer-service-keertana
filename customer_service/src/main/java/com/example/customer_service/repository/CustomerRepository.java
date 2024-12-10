package com.example.customer_service.repository;

import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email); // this method is to find customer by email
    Optional<Customer> findByPhoneNumber(String phoneNumber); // this method is to find customer by phone number
    @Query("SELECT c FROM Customer c WHERE " +
            "(:#{#criteria.id} IS NULL OR c.id = :#{#criteria.id}) AND " +
            "(:#{#criteria.name} IS NULL OR c.name LIKE %:#{#criteria.name}%) AND " +
            "(:#{#criteria.industry} IS NULL OR c.industry = :#{#criteria.industry}) AND " +
            "(:#{#criteria.companySize} IS NULL OR c.companySize = :#{#criteria.companySize}) AND " +
            "(:#{#criteria.email} IS NULL OR c.email LIKE %:#{#criteria.email}%) AND " +
            "(:#{#criteria.phoneNumber} IS NULL OR c.phoneNumber = :#{#criteria.phoneNumber}) AND " +
            "(:#{#criteria.address} IS NULL OR c.address LIKE %:#{#criteria.address}%) AND " +
            "(:#{#criteria.status} IS NULL OR c.status = :#{#criteria.status})")
    Page<Customer> findCustomersByFilters(@Param("criteria") SearchCriteria criteria, Pageable pageable);

}
