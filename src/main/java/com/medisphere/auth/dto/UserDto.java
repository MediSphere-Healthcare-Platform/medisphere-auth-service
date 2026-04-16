package com.medisphere.auth.dto;

import lombok.Data;

@Data
public class UserDto {
    private String msUserId;
    private String email;
    private String password;
    private String role;
    
    // Extra fields passed from Admin Service for Doctor sync
    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
    private String licenseUrl;
}
