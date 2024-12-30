package com.example.customer_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtRequestFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtRequestFilter = new JwtRequestFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext(); // Clear the SecurityContext before each test
    }


    @Test
    void testDoFilterInternal_WithIncompleteBearerToken() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer ");

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WithNonBearerAuthorizationHeader() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic some-token");

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WhenJwtUtilThrowsException() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.extractEmail("valid-token")).thenThrow(new RuntimeException("JWT parsing error"));

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WhenExtractEmailReturnsNull() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.extractEmail("valid-token")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_TokenWithNoUserDetails() throws Exception {
        String validToken = "valid-token";
        String email = "user@example.com";

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(null);

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WhenSecurityContextAlreadySet() throws Exception {
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.extractEmail("valid-token")).thenReturn("user@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("pre-authenticated-user", null, Collections.emptyList())
        );

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertEquals("pre-authenticated-user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WithValidTokenAndAuthenticationSet() throws Exception {
        // Mock valid token and user details
        String validToken = "valid-token";
        String email = "user@example.com";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                "password",
                Collections.emptyList()
        );

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, email)).thenReturn(true);

        // Execute the filter
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Verify that the authentication is set in the SecurityContext
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authToken, "Authentication should not be null");
        assertEquals(userDetails, authToken.getPrincipal(), "Authentication principal should match the userDetails");

        // Compare the authorities' contents
        assertIterableEquals(userDetails.getAuthorities(), authToken.getAuthorities(), "Authorities should match");

        // Ensure the filter chain continues
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
    @Test
    void testDoFilterInternal_ValidTokenSetsAuthentication() throws Exception {
        // Mock valid token and user details
        String validToken = "valid-token";
        String email = "user@example.com";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email,
                "password",
                Collections.emptyList()
        );

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails.getUsername())).thenReturn(true);

        // Execute the filter
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Verify the SecurityContextHolder is updated with the correct authentication token
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authToken, "Authentication token should not be null");
        assertEquals(userDetails, authToken.getPrincipal(), "Principal should match the user details");
        assertIterableEquals(userDetails.getAuthorities(), authToken.getAuthorities(), "Authorities should match");

        // Ensure the filter chain continues
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
    @Test
    void testDoFilterInternal_ValidTokenAndCorrectAuthentication() throws Exception {
        // Arrange: Setup valid token, user details, and mocks
        String validToken = "valid-token";
        String email = "user@example.com";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "password", Collections.emptyList()
        );

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, userDetails.getUsername())).thenReturn(true);

        // Act: Execute the filter
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // Assert: Verify the SecurityContextHolder is updated correctly
        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authToken, "Authentication token should not be null");
        assertEquals(userDetails, authToken.getPrincipal(), "Principal should match the user details");
        assertNull(authToken.getCredentials(), "Credentials should be null");
        assertIterableEquals(userDetails.getAuthorities(), authToken.getAuthorities(), "Authorities should match");

        // Assert: Verify filter chain proceeds
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WithExpiredToken() throws Exception {
        String expiredToken = "expired-token";
        String email = "user@example.com";

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(jwtUtil.extractEmail(expiredToken)).thenReturn(email);
        when(jwtUtil.validateToken(expiredToken, email)).thenReturn(false); // Token is expired

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null for expired token");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        String invalidToken = "invalid-token";
        String email = "user@example.com";

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtil.extractEmail(invalidToken)).thenReturn(email);
        when(jwtUtil.validateToken(invalidToken, email)).thenReturn(false);

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be set for an invalid token");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testDoFilterInternal_WhenUserDetailsServiceThrowsException() throws Exception {
        String validToken = "valid-token";
        String email = "user@example.com";

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenThrow(new RuntimeException("User not found"));

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should be null for exceptions from UserDetailsService");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
    @Test
    void testDoFilterInternal_WithEmptyAuthorities() throws Exception {
        String validToken = "valid-token";
        String email = "user@example.com";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "password", Collections.emptyList() // Empty authorities
        );

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, email)).thenReturn(true);

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        UsernamePasswordAuthenticationToken authToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authToken, "Authentication token should not be null");
        assertTrue(authToken.getAuthorities().isEmpty(), "Authorities should be empty");
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }
    @Test
    void testDoFilterInternal_WithMultipleFilters() throws Exception {
        String validToken = "valid-token";
        String email = "user@example.com";
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                email, "password", Collections.emptyList()
        );

        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractEmail(validToken)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtUtil.validateToken(validToken, email)).thenReturn(true);

        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should not be null");
    }

}
