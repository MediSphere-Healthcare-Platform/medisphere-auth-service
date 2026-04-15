package com.medisphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPatientRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
}
