package com.examly.springapp.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String currency;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, String firstName, String lastName, String currency, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.currency = currency;
        this.roles = roles;
    }
}