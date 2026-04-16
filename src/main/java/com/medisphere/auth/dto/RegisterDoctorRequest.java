package com.medisphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDoctorRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String specialty;
    private String phone;
    private String licenseUrl;
}
