package com.medisphere.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorDTO {
    private String firstName;
    private String lastName;
    private String doctorId;
    private String msUserId;
    private String specialty;
    private String drLicence;
    private String drContactNo;
    private String drNic;
    private String status;
    private String profilePic;
    private Instant createDate;
    private Instant modifiedDate;
}
