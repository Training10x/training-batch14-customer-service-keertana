package com.example.customer_service.controller;

import com.example.customer_service.dto.AuthRequest;
import com.example.customer_service.dto.AuthResponse;
import com.example.customer_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdminLogin_Success() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("admin@example.com");
        authRequest.setPassword("adminPass123");

        // Mock roles and UserDetails
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        User userDetails = new User(authRequest.getEmail(), "adminPass123", authorities);

        // Mock Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Mock the authentication manager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Mock JWT generation
        String mockToken = "adminMockJwtToken";
        when(jwtUtil.generateToken(authRequest.getEmail(), List.of("ROLE_ADMIN")))
                .thenReturn(mockToken);

        // Act
        AuthResponse response = authController.login(authRequest);

        // Assert
        assertNotNull(response, "AuthResponse should not be null");
        assertEquals(mockToken, response.getToken(), "Token should match the mocked value");

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtUtil, times(1)).generateToken(authRequest.getEmail(), List.of("ROLE_ADMIN"));
    }


    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("user@example.com");
        authRequest.setPassword("wrongPassword");

        // Mock bad credentials exception
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act and Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authController.login(authRequest));
        assertEquals("Invalid credentials", exception.getMessage());

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verifyNoInteractions(jwtUtil);
    }


}
