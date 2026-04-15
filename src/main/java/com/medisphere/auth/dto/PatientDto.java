package com.medisphere.auth.dto;

import lombok.Data;

@Data
public class PatientDto {
    private String msUserId;
    private String name;
    private String phone;
    private String email;
}
