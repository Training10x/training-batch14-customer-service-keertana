package com.example.customer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;

    public String getToken() {
        return token;
    }
     public void setToken(String token) {
        this.token = token;
    }
}
