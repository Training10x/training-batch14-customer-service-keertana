package com.example.customer_service.service;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.exception.DuplicateResourceException;
import com.example.customer_service.mapper.CustomerMapper;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_shouldThrowExceptionWhenEmailExists() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setEmail("0Hb0v@example.com");

        when(customerRepository.findByEmail("0Hb0v@example.com"))
                .thenReturn(Optional.of(new Customer()));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> customerServiceImpl.createCustomer(customerDTO)
        );

        assertEquals("This Email ID already exists", exception.getMessage());
        verify(customerRepository).findByEmail("0Hb0v@example.com");
    }

    @Test
    void createCustomer_shouldThrowExceptionWhenPhoneNumberExists() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setEmail("unique@example.com");
        customerDTO.setPhoneNumber("1234567890");

        when(customerRepository.findByEmail("unique@example.com"))
                .thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.of(new Customer()));

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> customerServiceImpl.createCustomer(customerDTO)
        );

        assertEquals("Phone number already exists", exception.getMessage());
        verify(customerRepository).findByEmail("unique@example.com");
        verify(customerRepository).findByPhoneNumber("1234567890");
    }

    @Test
    void createCustomer_shouldSaveCustomerWhenEmailAndPhoneAreUnique() {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setEmail("unique@example.com");
        customerDTO.setPhoneNumber("1234567890");

        Customer customer = new Customer();
        customer.setEmail("unique@example.com");

        when(customerRepository.findByEmail("unique@example.com"))
                .thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber("1234567890"))
                .thenReturn(Optional.empty());
        when(customerMapper.toEntity(customerDTO))
                .thenReturn(customer);
        when(customerRepository.save(customer))
                .thenReturn(customer);
        when(customerMapper.toDto(customer))
                .thenReturn(customerDTO);

        // Act
        CustomerDTO result = customerServiceImpl.createCustomer(customerDTO);

        // Assert
        assertEquals(customerDTO, result);
        verify(customerRepository, times(1)).findByEmail("unique@example.com");
        verify(customerRepository, times(1)).findByPhoneNumber("1234567890");
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void isEmailDuplicate_shouldReturnTrueWhenEmailExists() {
        // Arrange
        String existingEmail = "test@example.com";
        Customer mockCustomer = new Customer(); // Create a mock customer object
        when(customerRepository.findByEmail(existingEmail)).thenReturn(Optional.of(mockCustomer));

        // Act
        boolean result = customerServiceImpl.isEmailDuplicate(existingEmail);

        // Assert
        assertTrue(result, "Email should be marked as duplicate");
        verify(customerRepository, times(1)).findByEmail(existingEmail); // Ensure the repository method is called
    }
    @Test
    void isPhoneDuplicate_shouldReturnTrueWhenPhoneNumberExists() {
        // Arrange
        String existingPhoneNumber = "1234567890";
        Customer mockCustomer = new Customer(); // Create a mock customer object
        when(customerRepository.findByPhoneNumber(existingPhoneNumber)).thenReturn(Optional.of(mockCustomer));

        // Act
        boolean result = customerServiceImpl.isPhoneDuplicate(existingPhoneNumber);

        // Assert
        assertTrue(result, "Phone number should be marked as duplicate");
        verify(customerRepository, times(1)).findByPhoneNumber(existingPhoneNumber); // Ensure the repository method is called
    }

    @Test
    void getCustomerById_shouldReturnCustomerWhenCustomerExists() {
        // Arrange
        Long existingCustomerId = 1L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(existingCustomerId);
        when(customerRepository.findById(existingCustomerId)).thenReturn(Optional.of(mockCustomer));

        // Act
        Customer result = customerServiceImpl.getCustomerById(existingCustomerId);

        // Assert
        assertNotNull(result, "Customer should not be null");
        assertEquals(existingCustomerId, result.getId(), "Customer ID should match");
        verify(customerRepository, times(1)).findById(existingCustomerId);
    }

    @Test
    void getCustomerById_shouldReturnNullWhenCustomerDoesNotExist() {
        // Arrange
        Long nonExistentCustomerId = 2L;
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        // Act
        Customer result = customerServiceImpl.getCustomerById(nonExistentCustomerId);

        // Assert
        assertNull(result, "Customer should be null when not found");
        verify(customerRepository, times(1)).findById(nonExistentCustomerId);
    }
    @Test
    void findAllCustomers_shouldReturnListOfCustomers() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(1L);
        Customer customer2 = new Customer();
        customer2.setId(2L);
        List<Customer> mockCustomers = List.of(customer1, customer2);

        when(customerRepository.findAll()).thenReturn(mockCustomers);

        // Act
        List<Customer> result = customerServiceImpl.findAllCustomers();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Result should contain 2 customers");
        assertEquals(mockCustomers, result, "Result should match the mock customers");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void hasPermission_shouldReturnTrueForValidPermission() {
        // Arrange
        String validPermission = "CREATE_CUSTOMER";

        // Act
        boolean result = customerServiceImpl.hasPermission(validPermission);

        // Assert
        assertTrue(result, "Expected true for valid permission: " + validPermission);
    }

    @Test
    void hasPermission_shouldReturnFalseForInvalidPermission() {
        // Arrange
        String invalidPermission = "DELETE_CUSTOMER";

        // Act
        boolean result = customerServiceImpl.hasPermission(invalidPermission);

        // Assert
        assertFalse(result, "Expected false for invalid permission: " + invalidPermission);
    }

    @Test
    void hasPermission_shouldReturnTrueForAllValidPermissions() {
        // Arrange
        List<String> validPermissions = List.of("CREATE_CUSTOMER", "SEARCH_CUSTOMER", "UPDATE_CUSTOMER_STATUS", "UPDATE_CUSTOMER");

        // Act & Assert
        for (String permission : validPermissions) {
            assertTrue(customerServiceImpl.hasPermission(permission), "Expected true for valid permission: " + permission);
        }
    }

    @Test
    void hasPermission_shouldReturnFalseForEmptyOrNullPermission() {
        // Arrange
        String emptyPermission = "";
        String nullPermission = null;

        // Act & Assert
        assertFalse(customerServiceImpl.hasPermission(emptyPermission), "Expected false for empty permission");
        assertFalse(customerServiceImpl.hasPermission(nullPermission), "Expected false for null permission");
    }

    @Test
    void isAuthenticated_shouldReturnTrue() {
        // Act
        boolean result = customerServiceImpl.isAuthenticated();

        when(customerService.isAuthenticated()).thenReturn(true);

        // Assert
        assertTrue(result, "Expected true as the user is always authenticated.");
    }

    @Test
    void customerExists_shouldReturnTrueWhenCustomerExists() {
        // Arrange
        Long existingCustomerId = 1L;
        when(customerRepository.findById(existingCustomerId)).thenReturn(Optional.of(new Customer()));

        // Act
        boolean result = customerServiceImpl.customerExists(existingCustomerId);

        // Assert
        assertTrue(result, "Expected true as the customer exists in the repository.");
        verify(customerRepository, times(1)).findById(existingCustomerId);
    }

    @Test
    void updateCustomerStatus_shouldReturnTrueWhenStatusIsEnabled() {
        // Arrange
        Long customerId = 1L;
        String newStatus = "enabled";

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setStatus("disabled");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        // Act
        boolean result = customerServiceImpl.updateCustomerStatus(customerId, newStatus);

        // Assert
        assertTrue(result, "Expected true as the status update was successful.");
        assertEquals(newStatus, existingCustomer.getStatus(), "The status should be updated to 'enabled'.");
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(existingCustomer);
    }
    @Test
    void updateCustomerStatus_shouldReturnTrueWhenStatusIsDisabled() {
        // Arrange
        Long customerId = 1L;
        String newStatus = "disabled";

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setStatus("enabled");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        // Act
        boolean result = customerServiceImpl.updateCustomerStatus(customerId, newStatus);

        // Assert
        assertTrue(result, "Expected true as the status update was successful.");
        assertEquals(newStatus, existingCustomer.getStatus(), "The status should be updated to 'disabled'.");
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(existingCustomer);
    }

    @Test
    void updateCustomerStatus_shouldReturnFalseWhenCustomerDoesNotExist() {
        // Arrange
        Long customerId = 2L;
        String newStatus = "enabled";

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act
        boolean result = customerServiceImpl.updateCustomerStatus(customerId, newStatus);

        // Assert
        assertFalse(result, "Expected false as the customer does not exist.");
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_shouldUpdateCustomerSuccessfully() {
        // Arrange
        Long customerId = 1L;
        Map<String, Object> updates = Map.of("name", "Updated Name", "address", "Updated Address");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setName("Old Name");
        existingCustomer.setAddress("Old Address");

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(customerId);
        updatedCustomer.setName("Updated Name");
        updatedCustomer.setAddress("Updated Address");

        CustomerDTO updatedCustomerDTO = new CustomerDTO();
        updatedCustomerDTO.setName("Updated Name");
        updatedCustomerDTO.setAddress("Updated Address");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(updatedCustomer);
        when(customerMapper.toDto(updatedCustomer)).thenReturn(updatedCustomerDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(updatedCustomerDTO, result, "Customer should be updated successfully.");
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).save(existingCustomer);
    }
    @Test
    void updateCustomer_shouldThrowExceptionWhenUpdatingEmailWithConflict() {
        // Arrange
        Long customerId = 1L;
        Map<String, Object> updates = Map.of("email", "newEmail@example.com");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setEmail("oldEmail@example.com");

        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setEmail("newEmail@example.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail("newEmail@example.com")).thenReturn(Optional.of(otherCustomer));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerServiceImpl.updateCustomer(customerId, updates));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode(), "Should return CONFLICT for email conflict.");
        assertEquals("Email already in use", exception.getReason(), "Exception reason should indicate email conflict.");
        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).findByEmail("newEmail@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }
    @Test
    void updateCustomer_shouldNotThrowExceptionWhenEmailIsSame() {
        // Arrange
        Long customerId = 1L;
        String existingEmail = "sameEmail@example.com";

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setEmail(existingEmail);

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customerId);
        customerDTO.setEmail(existingEmail);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);
        when(customerMapper.toDto(existingCustomer)).thenReturn(customerDTO);

        // Act
        CustomerDTO updatedCustomer = customerServiceImpl.updateCustomer(customerId, Map.of("email", existingEmail));

        // Assert
        verify(customerRepository, never()).findByEmail(existingEmail); // Ensure no repository call for email lookup
        verify(customerRepository, times(1)).save(existingCustomer); // Ensure the customer is saved
        assertEquals(existingCustomer.getEmail(), updatedCustomer.getEmail());
    }

    @Test
    void updateCustomer_shouldUpdateWhenEmailIsUnique() {
        // Arrange
        Long customerId = 1L;
        String existingEmail = "oldEmail@example.com";
        String newEmail = "uniqueEmail@example.com";
        Map<String, Object> updates = Map.of("email", newEmail);

        Customer existingCustomer = new Customer();
        existingCustomer.setEmail(existingEmail);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerDTO expectedDTO = new CustomerDTO();
        expectedDTO.setEmail(newEmail);
        when(customerMapper.toDto(existingCustomer)).thenReturn(expectedDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(expectedDTO, result);
        verify(customerRepository, times(1)).findByEmail(newEmail);
        verify(customerRepository, times(1)).save(existingCustomer);
    }
    @Test
    void updateCustomer_shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        Long customerId = 1L;
        String newEmail = "existingEmail@example.com";
        Map<String, Object> updates = Map.of("email", newEmail);

        Customer existingCustomer = new Customer();
        existingCustomer.setEmail("oldEmail@example.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByEmail(newEmail)).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerServiceImpl.updateCustomer(customerId, updates));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(customerRepository, times(1)).findByEmail(newEmail);
    }


    @Test
    void updateCustomer_shouldNotThrowExceptionWhenPhoneNumberIsSame() {
        // Arrange
        Long customerId = 1L;
        String existingPhoneNumber = "1234567890";
        Map<String, Object> updates = Map.of("phoneNumber", existingPhoneNumber);

        Customer existingCustomer = new Customer();
        existingCustomer.setPhoneNumber(existingPhoneNumber);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);
        CustomerDTO expectedDTO = new CustomerDTO();
        expectedDTO.setPhoneNumber(existingPhoneNumber);
        when(customerMapper.toDto(existingCustomer)).thenReturn(expectedDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(expectedDTO, result, "Updating to the same phone number should not throw an exception.");
        verify(customerRepository, never()).findByPhoneNumber(existingPhoneNumber);
        verify(customerRepository, times(1)).save(existingCustomer);
    }

    @Test
    void updateCustomer_shouldThrowExceptionWhenPhoneNumberAlreadyExists() {
        // Arrange
        Long customerId = 1L;
        String newPhoneNumber = "9876543210";
        Map<String, Object> updates = Map.of("phoneNumber", newPhoneNumber);

        Customer existingCustomer = new Customer();
        existingCustomer.setPhoneNumber("1234567890");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(Optional.of(new Customer()));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> customerServiceImpl.updateCustomer(customerId, updates));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode(), "Should return CONFLICT for phone number conflict.");
        assertEquals("Phone number already in use", exception.getReason(), "Exception reason should indicate phone conflict.");
        verify(customerRepository, times(1)).findByPhoneNumber(newPhoneNumber);
        verify(customerRepository, never()).save(existingCustomer);
    }
    @Test
    void updateCustomer_shouldUpdateWhenPhoneNumberIsUnique() {
        // Arrange
        Long customerId = 1L;
        String existingPhoneNumber = "1234567890";
        String newPhoneNumber = "9876543210";
        Map<String, Object> updates = Map.of("phoneNumber", newPhoneNumber);

        Customer existingCustomer = new Customer();
        existingCustomer.setPhoneNumber(existingPhoneNumber);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.findByPhoneNumber(newPhoneNumber)).thenReturn(Optional.empty());
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerDTO expectedDTO = new CustomerDTO();
        expectedDTO.setPhoneNumber(newPhoneNumber);
        when(customerMapper.toDto(existingCustomer)).thenReturn(expectedDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(expectedDTO, result, "Customer phone number should be updated when it is unique.");
        verify(customerRepository, times(1)).findByPhoneNumber(newPhoneNumber); // Verify the check for unique phone number
        verify(customerRepository, times(1)).save(existingCustomer); // Verify the save operation
    }
    @Test
    void updateCustomer_shouldUpdateIndustry() {
        // Arrange
        Long customerId = 1L;
        String newIndustry = "Technology";
        Map<String, Object> updates = Map.of("industry", newIndustry);

        Customer existingCustomer = new Customer();
        existingCustomer.setIndustry("Finance");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerDTO expectedDTO = new CustomerDTO();
        expectedDTO.setIndustry(newIndustry);
        when(customerMapper.toDto(existingCustomer)).thenReturn(expectedDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(expectedDTO, result, "Customer industry should be updated.");
        assertEquals(newIndustry, existingCustomer.getIndustry(), "Industry should be updated in the customer object.");
        verify(customerRepository, times(1)).findById(customerId); // Verify that the customer is fetched
        verify(customerRepository, times(1)).save(existingCustomer); // Verify that the customer is saved
    }
    @Test
    void updateCustomer_shouldUpdateCompanySize() {
        // Arrange
        Long customerId = 1L;
        String newCompanySize = "500-1000";
        Map<String, Object> updates = Map.of("companySize", newCompanySize);

        Customer existingCustomer = new Customer();
        existingCustomer.setCompanySize("100");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);

        CustomerDTO expectedDTO = new CustomerDTO();
        expectedDTO.setCompanySize(newCompanySize);
        when(customerMapper.toDto(existingCustomer)).thenReturn(expectedDTO);

        // Act
        CustomerDTO result = customerServiceImpl.updateCustomer(customerId, updates);

        // Assert
        assertEquals(expectedDTO, result, "Customer company size should be updated.");
        assertEquals(newCompanySize, existingCustomer.getCompanySize(), "Company size should be updated in the customer object.");
        verify(customerRepository, times(1)).findById(customerId); // Verify that the customer is fetched
        verify(customerRepository, times(1)).save(existingCustomer); // Verify that the customer is saved
    }
    @Test
    void updateCustomer_shouldThrowExceptionForInvalidField() {
        // Arrange
        Long customerId = 1L;
        String invalidFieldName = "unknownField";
        Map<String, Object> updates = Map.of(invalidFieldName, "someValue");

        Customer existingCustomer = new Customer();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerServiceImpl.updateCustomer(customerId, updates),
                "Should throw ResponseStatusException for invalid field"
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode(), "Should return UNPROCESSABLE_ENTITY status");
        assertTrue(
                exception.getReason().contains(invalidFieldName),
                "Exception reason should include the invalid field name"
        );

        verify(customerRepository, times(1)).findById(customerId); // Ensure the customer is fetched
        verify(customerRepository, never()).save(any(Customer.class)); // Ensure save is not called
    }

    @Test
    void searchCustomers_shouldReturnResultsForValidCriteria() {
        // Arrange
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(0);
        criteria.setLimit(5);

        Customer customer = new Customer();
        customer.setId(1L);
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L);

        Page<Customer> customerPage = new PageImpl<>(List.of(customer));
        when(customerRepository.findCustomersByFilters(eq(criteria), any(Pageable.class))).thenReturn(customerPage);
        when(customerMapper.toDto(customer)).thenReturn(customerDTO);

        // Act
        Map<String, Object> result = customerServiceImpl.searchCustomers(criteria);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.get("total_count"), "Total count should match");
        assertEquals(1, result.get("page_count"), "Page count should match");
        assertEquals(0, result.get("current_page"), "Current page should match");
        assertEquals(List.of(customerDTO), result.get("results"), "Results should match the mapped DTOs");
        verify(customerRepository, times(1)).findCustomersByFilters(eq(criteria), any(Pageable.class));
    }

    @Test
    void searchCustomers_shouldHandleEmptyResults() {
        // Arrange
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(0);
        criteria.setLimit(5);

        Page<Customer> emptyPage = new PageImpl<>(Collections.emptyList());
        when(customerRepository.findCustomersByFilters(eq(criteria), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        Map<String, Object> result = customerServiceImpl.searchCustomers(criteria);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(0L, result.get("total_count"), "Total count should be 0");
        assertEquals(0, result.get("page_count"), "Page count should be 0");
        assertEquals(0, result.get("current_page"), "Current page should be 0");
        assertTrue(((List<?>) result.get("results")).isEmpty(), "Results should be empty");
        verify(customerRepository, times(1)).findCustomersByFilters(eq(criteria), any(Pageable.class));
    }

    @Test
    void searchCustomers_shouldThrowBadRequestForInvalidCriteria() {
        // Arrange
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(-1); // Invalid page number
        criteria.setLimit(5);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            customerServiceImpl.searchCustomers(criteria);
        });

        assertEquals("Invalid search parameters", exception.getReason(), "Should return appropriate error message");
        assertEquals(400, exception.getStatusCode().value(), "Should return BAD_REQUEST status code");
        verify(customerRepository, never()).findCustomersByFilters(any(), any());
    }

    @Test
    void searchCustomers_shouldHandleRepositoryException() {
        // Arrange
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(0);
        criteria.setLimit(5);

        when(customerRepository.findCustomersByFilters(eq(criteria), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            customerServiceImpl.searchCustomers(criteria);
        });

        assertEquals("Failed to search customers", exception.getReason(), "Should return appropriate error message");
        assertEquals(500, exception.getStatusCode().value(), "Should return INTERNAL_SERVER_ERROR status code");
        verify(customerRepository, times(1)).findCustomersByFilters(eq(criteria), any(Pageable.class));
    }

    @Test
    void searchCustomers_shouldReturnEmptyWhenNoContent() {
        // Arrange
        SearchCriteria criteria = new SearchCriteria();
        criteria.setPage(0);
        criteria.setLimit(10);

        when(customerRepository.findCustomersByFilters(eq(criteria), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        Map<String, Object> result = customerServiceImpl.searchCustomers(criteria);

        // Assert
        assertNotNull(result, "Response map should not be null");
        assertEquals(0L, result.get("total_count"), "Total count should be 0");
        assertEquals(0, result.get("page_count"), "Page count should be 0");
        assertEquals(0, result.get("current_page"), "Current page should be 0");
        assertTrue(((List<?>) result.get("results")).isEmpty(), "Results list should be empty");
        verify(customerRepository, times(1)).findCustomersByFilters(eq(criteria), any(Pageable.class));
    }



}
