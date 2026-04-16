package com.medisphere.auth.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class PatientDto {
    private String msUserId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String bloodGroup;
    private String allergies;
    private String chronicConditions;
}
