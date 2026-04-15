package com.medisphere.auth.service;

import com.medisphere.auth.dto.*;
import com.medisphere.auth.entity.User;
import com.medisphere.auth.repository.UserRepository;
import com.medisphere.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RestTemplate restTemplate = new RestTemplate();

    public ApiResponse<String> registerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("Email already exists")
                    .build();
        }

        User user = new User();
        user.setMsUserId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PATIENT");
        user.setCreatedDate(Instant.now());
        user.setModifiedDate(Instant.now());

        userRepository.save(user);

        // Call Patient Service
        try {
            // Assuming Patient Service structure
            PatientDto patientDto = new PatientDto();
            patientDto.setMsUserId(user.getMsUserId());
            patientDto.setName(request.getName());
            patientDto.setPhone(request.getPhone());
            patientDto.setEmail(request.getEmail());

            restTemplate.postForEntity("http://patient-service/patients", patientDto, Object.class);
        } catch (Exception e) {
            // Log error but user is created in Auth DB
            // In real scenario, might need transactional consistency or compensating actions
            System.err.println("Failed to call Patient Service: " + e.getMessage());
        }

        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("Patient registered successfully")
                .data(user.getMsUserId())
                .build();
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ApiResponse.<AuthResponse>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(AuthResponse.builder()
                        .token(token)
                        .role(user.getRole())
                        .message("Login successful")
                        .build())
                .build();
    }

    public ApiResponse<String> registerDoctor(RegisterDoctorRequest request) {
        // Forward to Admin Service for pending approval
        try {
            restTemplate.postForEntity("http://admin-service/api/v1/admin/doctors/pending", request, Object.class);
            return ApiResponse.<String>builder()
                    .status("SUCCESS")
                    .message("Doctor registration submitted for approval")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("Failed to submit doctor registration: " + e.getMessage())
                    .build();
        }
    }

    public ApiResponse<String> createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("User already exists")
                    .build();
        }

        User user = new User();
        user.setMsUserId(userDto.getMsUserId() != null ? userDto.getMsUserId() : UUID.randomUUID().toString());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(userDto.getRole());
        user.setCreatedDate(Instant.now());
        user.setModifiedDate(Instant.now());

        userRepository.save(user);

        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("User created successfully")
                .data(user.getMsUserId())
                .build();
    }
}
