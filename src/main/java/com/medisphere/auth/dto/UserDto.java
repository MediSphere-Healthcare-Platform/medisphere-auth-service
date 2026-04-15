package com.medisphere.auth.dto;

import lombok.Data;

@Data
public class UserDto {
    private String msUserId;
    private String email;
    private String password;
    private String role;
}
