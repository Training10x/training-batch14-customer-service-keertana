package com.example.customer_service.controller;

import com.example.customer_service.dto.CustomerDTO;
import com.example.customer_service.dto.SearchCriteria;
import com.example.customer_service.entity.Customer;
import com.example.customer_service.repository.CustomerRepository;
import com.example.customer_service.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@ExtendWith(MockitoExtension.class)
class CustomerServiceControllerTest {
    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;
    private static final String UNAUTHORIZED = "Unauthorized access";
    private static final String FORBIDDEN = "Forbidden";
    private static final String NOT_FOUND = "Customer not found";
    private static final String ENABLED = "enabled";

    private ObjectMapper objectMapper;


    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    void createCustomer_shouldReturnCreateStatus() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("Johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);
        when(customerService.isEmailDuplicate(any(String.class))).thenReturn(false);
        when(customerService.isPhoneDuplicate(any(String.class))).thenReturn(false);
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customerDTO);

        mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created successfully"));

        System.out.println("Test Passed: POST Create test passed successfully.");
    }

    @Test
    void createCustomer_shouldReturnUnauthorized() throws Exception {

        when(customerService.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Empty body
                .andExpect(status().isUnauthorized()) // Expect 401 Unauthorized
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        System.out.println("Test Passed: POST Unauthorized scenario tested successfully.");
    }
    @Test
    void createCustomer_shouldReturnForbidden() throws Exception {

        when(customerService.isAuthenticated()).thenReturn(true);

        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(false);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Empty body
                .andExpect(status().isForbidden()) // Expect 403 Forbidden
                .andExpect(jsonPath("$.message").value(FORBIDDEN))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        System.out.println("Test Passed: POST Forbidden scenario tested successfully.");
    }
    @Test
    void createCustomer_shouldReturnNotFoundWhenIdIsInvalid() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(999L); // Non-existing ID
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        // Simulate authentication, permission, and repository behavior
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);
        when(customerRepository.findById(999L)).thenReturn(Optional.empty()); // ID not found

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isNotFound()) // Expect 404 Not Found
                .andExpect(jsonPath("$.message").value(NOT_FOUND))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        System.out.println("Test Passed: POST with invalid ID returned 404 Not Found.");
    }
    @Test
    void createCustomer_shouldSkipIdCheckWhenIdIsNull() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(null); // No ID
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.message").value("Customer created successfully"));

        System.out.println("Test Passed: POST with null ID created customer successfully.");
    }
    @Test
    void createCustomer_shouldPassWhenIdIsValid() throws Exception {
        // Arrange
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(1L); // Valid ID
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer())); // ID found
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customerDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(jsonPath("$.message").value("Customer created successfully"));

        System.out.println("Test Passed: POST with valid ID created customer successfully.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenEmailIsNull() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail(null); // Null email
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with null email returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenEmailIsEmpty() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail(" "); // Empty email
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with empty email returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenEmailLacksAtSymbol() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("invalidemail.com"); // Invalid email without '@'
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with email lacking '@' returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenPhoneNumberIsNull() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber(null); // Null phone number
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid phone number format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with null phone number returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenPhoneNumberIsEmpty() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber(" "); // Empty phone number
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid phone number format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with empty phone number returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnBadRequestWhenPhoneNumberIsInvalid() throws Exception {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("12345"); // Invalid phone number
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid phone number format"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: POST with invalid phone number format returned 400 Bad Request.");
    }
    @Test
    void createCustomer_shouldReturnConflictWhenEmailExists() throws Exception {
        // Arrange: Create a customerDTO with duplicate email
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com"); // Duplicate email
        customerDTO.setPhoneNumber("1234567890");
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        // Simulate customerService behavior
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);
        when(customerService.isEmailDuplicate(customerDTO.getEmail())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(jsonPath("$.message").value("Already exists"))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));

        System.out.println("Test Passed: POST with duplicate email returned 409 Conflict.");
    }
    @Test
    void createCustomer_shouldReturnConflictWhenPhoneNumberExists() throws Exception {
        // Arrange: Create a customerDTO with duplicate phone number
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setName("John Doe");
        customerDTO.setIndustry("IT");
        customerDTO.setCompanySize("50");
        customerDTO.setEmail("johndoe@example.com");
        customerDTO.setPhoneNumber("1234567890"); // Duplicate phone number
        customerDTO.setAddress("123 Main St");
        customerDTO.setStatus("enabled");

        // Simulate customerService behavior
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("CREATE_CUSTOMER")).thenReturn(true);
        when(customerService.isPhoneDuplicate(customerDTO.getPhoneNumber())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(jsonPath("$.message").value("Already exists"))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));

        System.out.println("Test Passed: POST with duplicate phone number returned 409 Conflict.");
    }

    @Test
    void getAllCustomers_shouldReturnCustomerList() throws Exception {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setName("John Doe");
        customer1.setIndustry("IT");
        customer1.setCompanySize("50");
        customer1.setEmail("johndoe@example.com");
        customer1.setPhoneNumber("1234567890");
        customer1.setAddress("123 Main St");
        customer1.setStatus("enabled");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setName("Jane Smith");
        customer2.setIndustry("Finance");
        customer2.setCompanySize("200");
        customer2.setEmail("janesmith@example.com");
        customer2.setPhoneNumber("9876543210");
        customer2.setAddress("456 Elm St");
        customer2.setStatus("enabled");

        List<Customer> customers = List.of(customer1, customer2);
        when(customerService.findAllCustomers()).thenReturn(customers);

        mockMvc.perform(get("/api/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers[0].id").value(1))
                .andExpect(jsonPath("$.customers[0].name").value("John Doe"))
                .andExpect(jsonPath("$.customers[1].id").value(2))
                .andExpect(jsonPath("$.customers[1].name").value("Jane Smith"));
    }
    @Test
    void getCustomerById_shouldReturnCustomer() throws Exception {
        // Mock data
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("John Doe");
        customer.setIndustry("IT");
        customer.setCompanySize("50");
        customer.setEmail("johndoe@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setAddress("123 Main St");
        customer.setStatus("enabled");

        // Mock service call
        when(customerService.getCustomerById(customerId)).thenReturn(customer);

        // Perform GET request
        mockMvc.perform(get("/api/v1/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.customer.id").value(customerId))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.customer.industry").value("IT"));

        System.out.println("Test Passed: GET Customer by ID returned successfully.");
    }

    @Test
    void searchCustomers_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Mock the service to return false for isAuthenticated
        when(customerService.isAuthenticated()).thenReturn(false);
        // Perform the GET request with search criteria
        mockMvc.perform(get("/api/v1/customers/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // Expect 401 Unauthorized
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        System.out.println("Test Passed: Search endpoint returned 401 Unauthorized.");
    }

    @Test
    void searchCustomers_shouldReturnForbiddenWhenPermissionDenied() throws Exception {
        // Mock the service to return true for isAuthenticated but false for hasPermission
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(false);

        mockMvc.perform(get("/api/v1/customers/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // Expect 403 Forbidden
                .andExpect(jsonPath("$.message").value(FORBIDDEN))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        System.out.println("Test Passed: Search endpoint returned 403 Forbidden due to lack of permissions.");
    }

    @Test
    void searchCustomers_shouldReturnBadRequestForInvalidPage() throws Exception {
        // Mock customer service methods
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(true);

        // Perform the request with the corrected logic
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "-1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid search parameter"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: Search endpoint returned 400 Bad Request for invalid page.");
    }
    @Test
    void searchCustomers_shouldReturnBadRequestForInvalidLimit() throws Exception {
        // Mock customer service methods
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(true);

        // Perform the request with limit = -1 (invalid)
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "0")
                        .param("limit", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid search parameter"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: Search endpoint returned 400 Bad Request for limit = -1.");

        // Perform the request with limit = 0 (invalid)
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "0")
                        .param("limit", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid search parameter"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: Search endpoint returned 400 Bad Request for limit = 0.");
    }

    @Test
    void searchCustomers_shouldReturnResultsForValidCriteria() throws Exception {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPage(1);
        searchCriteria.setLimit(10);

        Map<String, Object> result = Map.of("customers", List.of(new Customer(), new Customer()));

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(true);
        when(customerService.searchCustomers(any(SearchCriteria.class))).thenReturn(result);

        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "1")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers").isArray());

        System.out.println("Test Passed: Valid search criteria returned results.");
    }

    @Test
    void searchCustomers_shouldReturnBadRequestForIllegalArgumentException() throws Exception {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPage(0); // Valid page
        searchCriteria.setLimit(10); // Valid limit

        // Mock the customer service methods
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(true);
        when(customerService.searchCustomers(any(SearchCriteria.class)))
                .thenThrow(new IllegalArgumentException("Invalid search parameters"));

        // Perform the request
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "0")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid search parameter"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        System.out.println("Test Passed: Search endpoint returned 400 Bad Request for IllegalArgumentException.");
    }
    @Test
    void searchCustomers_shouldReturnInternalServerErrorForException() throws Exception {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPage(0); // Valid page
        searchCriteria.setLimit(10); // Valid limit

        // Mock the customer service methods
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("SEARCH_CUSTOMER")).thenReturn(true);
        when(customerService.searchCustomers(any(SearchCriteria.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Perform the request
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("page", "0")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to search customers"))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        System.out.println("Test Passed: Search endpoint returned 500 Internal Server Error for Exception.");
    }

    @Test
    void patchCustomerStatus_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {

        when(customerService.isAuthenticated()).thenReturn(false);

        Map<String, String> updates = Map.of("status", "active");

        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize with ObjectMapper
                .andExpect(status().isUnauthorized()) // Expecting 401 Unauthorized
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED)) // Match error message
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value())); // Match status code

        System.out.println("Test Passed: Unauthorized access tested successfully.");
    }

    @Test
    void patchCustomerStatus_shouldReturnForbiddenWhenNoPermission() throws Exception {

        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(false);

        Map<String, String> updates = Map.of("status", "active");

        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize with ObjectMapper
                .andExpect(status().isForbidden()) // Expecting 403 Forbidden
                .andExpect(jsonPath("$.message").value(FORBIDDEN)) // Match error message
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value())); // Match status code

        System.out.println("Test Passed: Forbidden access tested successfully.");
    }

    @Test
    void patchCustomerStatus_shouldReturnBadRequestWhenStatusFieldIsMissing() throws Exception {
        // Arrange: Create the request body without the 'status' key
        Map<String, String> updates = Map.of("name", "John Doe");

        // Mock the required service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize updates as JSON
                .andExpect(status().isBadRequest()) // Expecting 400 BAD REQUEST
                .andExpect(jsonPath("$.message").value("Status field is required")) // Match error message
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value())); // Match status code

        System.out.println("Test Passed: PATCH missing 'status' field returned 400 Bad Request.");
    }
    @Test
    void patchCustomerStatus_shouldReturnBadRequestForInvalidStatus() throws Exception {
        // Arrange: Prepare a request body with a status that's neither "enabled" nor "disabled"
        Map<String, String> updates = Map.of("status", "invalid_status");

        // Mock the necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize updates as JSON
                .andExpect(status().isBadRequest()) // Expecting 400 BAD REQUEST
                .andExpect(jsonPath("$.message").value("Invalid status value")) // Match the error message
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value())); // Match the status code

        System.out.println("Test Passed: PATCH with invalid status returned 400 Bad Request.");
    }

    @Test
    void patchCustomerStatus_shouldReturnOkWhenStatusIsEnabled() throws Exception {
        // Arrange: Prepare a valid request body with "enabled" status
        Map<String, String> updates = Map.of("status", "enabled");

        // Mock the necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);
        when(customerService.updateCustomerStatus(1L, "enabled")).thenReturn(true);

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk()) // Expecting 200 OK
                .andExpect(jsonPath("$.message").value("Customer status updated successfully")) // Match success message
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value())); // Match status code

        System.out.println("Test Passed: PATCH with valid 'enabled' status returned 200 OK.");
    }

    @Test
    void patchCustomerStatus_shouldReturnOkWhenStatusIsDisabled() throws Exception {
        // Arrange: Prepare a valid request body with "disabled" status
        Map<String, String> updates = Map.of("status", "disabled");

        // Mock the necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);
        when(customerService.updateCustomerStatus(1L, "disabled")).thenReturn(true);

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk()) // Expecting 200 OK
                .andExpect(jsonPath("$.message").value("Customer status updated successfully")) // Match success message
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value())); // Match status code

        System.out.println("Test Passed: PATCH with valid 'disabled' status returned 200 OK.");
    }


    @Test
    void patchCustomerStatus_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        // Arrange: Create a valid request body
        Map<String, String> updates = Map.of("status", ENABLED); // Use the static variable

        // Mock the required service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(false); // Simulate customer does not exist

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize updates as JSON
                .andExpect(status().isNotFound()) // Expecting 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Customer not found")) // Match error message
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value())); // Match status code

        System.out.println("Test Passed: PATCH with non-existent customer returned 404 Not Found.");
    }
    @Test
    void patchCustomerStatus_shouldReturnOkWhenUpdateIsSuccessful() throws Exception {
        // Arrange: Prepare valid customerId and request body
        Long customerId = 1L;
        Map<String, String> updatedStatus = Map.of("status", "enabled");

        // Mock required service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);
        when(customerService.customerExists(customerId)).thenReturn(true);
        when(customerService.updateCustomerStatus(customerId, "enabled")).thenReturn(true);

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStatus))) // Serialize updates as JSON
                .andExpect(status().isOk()) // Expecting 200 OK
                .andExpect(jsonPath("$.message").value("Customer status updated successfully")) // Match success message
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value())); // Match status code

        System.out.println("Test Passed: PATCH successfully updated customer status.");
    }
    @Test
    void patchCustomerStatus_shouldReturnInternalServerErrorWhenUpdateFails() throws Exception {
        // Arrange: Prepare valid customerId and request body
        Long customerId = 1L;
        Map<String, String> updates = Map.of("status", "enabled");

        // Mock required service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER_STATUS")).thenReturn(true);
        when(customerService.customerExists(customerId)).thenReturn(true);
        when(customerService.updateCustomerStatus(customerId, "enabled")).thenReturn(false); // Simulate failure

        // Act & Assert: Perform the PATCH request
        mockMvc.perform(patch("/api/v1/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))) // Serialize updates as JSON
                .andExpect(status().isInternalServerError()) // Expecting 500 INTERNAL SERVER ERROR
                .andExpect(jsonPath("$.message").value("Failed to update customer status")) // Match error message
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value())); // Match status code

        System.out.println("Test Passed: PATCH failed to update customer status and returned 500.");
    }

    @Test
    void updateCustomer_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Mock the service to simulate unauthenticated access
        when(customerService.isAuthenticated()).thenReturn(false);

        // Perform the PUT request with minimal input
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Minimal valid content
                .andExpect(status().isUnauthorized()) // Expecting 401 Unauthorized
                .andExpect(jsonPath("$.message").value(UNAUTHORIZED))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));

        System.out.println("Test Passed: PUT unauthorized access returned 401.");
    }

    @Test
    void updateCustomer_shouldReturnForbiddenWhenNoPermission() throws Exception {
        // Mock the service to simulate authenticated access but no permission
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(false);

        // Perform the PUT request with valid JSON body
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ACTIVE\"}")) // Add a valid JSON body
                .andExpect(status().isForbidden()) // Expecting 403 Forbidden
                .andExpect(jsonPath("$.message").value(FORBIDDEN))
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        System.out.println("Test Passed: PUT forbidden access returned 403.");
    }

    @Test
    void updateCustomer_shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        // Arrange: Mock the service to simulate a non-existing customer
        Long nonExistentCustomerId = 999L;
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(nonExistentCustomerId)).thenReturn(false);

        // Act & Assert: Perform the PUT request and expect 404 Not Found
        mockMvc.perform(put("/api/v1/customers/" + nonExistentCustomerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ENABLED\"}")) // Add a valid JSON body
                .andExpect(status().isNotFound()) // Expecting 404 Not Found
                .andExpect(jsonPath("$.message").value("Customer not found")) // Match the error message
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value())); // Match the status code

        System.out.println("Test Passed: PUT non-existent customer returned 404 Not Found.");
    }
    @Test
    void updateCustomer_shouldSkipEmailValidationWhenEmailKeyIsMissing() throws Exception {
        // Arrange: Prepare updates without "email"
        Map<String, Object> updates = Map.of("name", "John Doe");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and ensure no 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk()); // Expect success (not 400)

        System.out.println("Test Passed: PUT without 'email' key skipped email validation.");
    }
    @Test
    void updateCustomer_shouldReturnBadRequestWhenEmailIsNull() throws Exception {
        // Arrange: Prepare updates with a null "email"
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", null); // Allows null value

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid email format"));

        System.out.println("Test Passed: PUT with null 'email' returned 400 Bad Request.");
    }


    @Test
    void updateCustomer_shouldReturnBadRequestWhenEmailIsEmpty() throws Exception {
        // Arrange: Prepare updates with an empty "email"
        Map<String, Object> updates = Map.of("email", "");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid email format"));

        System.out.println("Test Passed: PUT with empty 'email' returned 400 Bad Request.");
    }
    @Test
    void updateCustomer_shouldReturnBadRequestWhenEmailLacksAtSymbol() throws Exception {
        // Arrange: Prepare updates with an invalid "email" missing "@"
        Map<String, Object> updates = Map.of("email", "invalidemail.com");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid email format"));

        System.out.println("Test Passed: PUT with invalid 'email' returned 400 Bad Request.");
    }
    @Test
    void updateCustomer_shouldPassWhenEmailIsValid() throws Exception {
        // Arrange: Prepare updates with a valid "email"
        Map<String, Object> updates = Map.of("email", "valid@example.com");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 200 OK
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk()); // Expecting success

        System.out.println("Test Passed: PUT with valid 'email' passed successfully.");
    }

    @Test
    void updateCustomer_shouldReturnBadRequestWhenPhoneNumberIsNull() throws Exception {
        // Arrange: Prepare updates with a null phone number
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", null);

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));

        System.out.println("Test Passed: PUT with null phoneNumber returned 400 Bad Request.");
    }
    @Test
    void updateCustomer_shouldReturnBadRequestWhenPhoneNumberIsEmpty() throws Exception {
        // Arrange: Prepare updates with an empty phone number
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));

        System.out.println("Test Passed: PUT with empty phoneNumber returned 400 Bad Request.");
    }
    @Test
    void updateCustomer_shouldReturnBadRequestWhenPhoneNumberIsInvalid() throws Exception {
        // Arrange: Prepare updates with an invalid phone number
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "12345abcde");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 400 Bad Request
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest()) // Expecting 400
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));

        System.out.println("Test Passed: PUT with invalid phoneNumber returned 400 Bad Request.");
    }
    @Test
    void updateCustomer_shouldReturnOkWhenPhoneNumberIsValid() throws Exception {
        // Arrange: Prepare updates with a valid phone number
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "1234567890");

        // Mock necessary service calls
        when(customerService.isAuthenticated()).thenReturn(true);
        when(customerService.hasPermission("UPDATE_CUSTOMER")).thenReturn(true);
        when(customerService.customerExists(1L)).thenReturn(true);

        // Act & Assert: Perform the PUT request and expect 200 OK
        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk()) // Expecting 200
                .andExpect(jsonPath("$.message").value("Customer updated successfully"));

        System.out.println("Test Passed: PUT with valid phoneNumber returned 200 OK.");
    }


}
