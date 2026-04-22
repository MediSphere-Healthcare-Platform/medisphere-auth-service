package com.medisphere.auth.service;

import com.medisphere.auth.client.AdminClient;
import com.medisphere.auth.client.NotificationClient;
import com.medisphere.auth.client.PatientClient;
import com.medisphere.auth.client.DoctorClient;
import com.medisphere.auth.dto.*;
import com.medisphere.auth.entity.User;
import com.medisphere.auth.repository.UserRepository;
import com.medisphere.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AdminClient adminClient;
    private final NotificationClient notificationClient;
    private final PatientClient patientClient;
    private final DoctorClient doctorClient;

    @Transactional
    public ApiResponse<String> registerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("Email already exists")
                    .build();
        }

        User user = new User();
        user.setMsUserId(generateMsUserId("PATIENT"));
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PATIENT");
        user.setCreatedDate(Instant.now());
        user.setModifiedDate(Instant.now());

        userRepository.save(user);

        // Call Patient Service via Feign Client - Propagate exception to trigger
        // rollback
        try {
            PatientDto patientDto = new PatientDto();
            patientDto.setMsUserId(user.getMsUserId());
            patientDto.setFirstName(request.getFirstName());
            patientDto.setLastName(request.getLastName());
            patientDto.setPhoneNumber(request.getPhone());
            // Provide short defaults so Patient Service DB constraints are not violated
            patientDto.setBloodGroup("N/A");
            patientDto.setAllergies("None");
            patientDto.setChronicConditions("None");

            patientClient.createPatient(patientDto);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Patient User created, but failed to sync with Patient Service: " + e.getMessage());
        }

        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("Patient registered successfully")
                .data(user.getMsUserId())
                .build();
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ApiResponse.<AuthResponse>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(AuthResponse.builder()
                        .token(token)
                        .role(user.getRole())
                        .msUserId(user.getMsUserId())
                        .message("Login successful")
                        .build())
                .build();
    }

    public ApiResponse<String> registerDoctor(RegisterDoctorRequest request) {
        // Forward to Admin Service for pending approval via Feign Client
        try {
            adminClient.registerDoctor(request);

            // Notify Admin via Email
            try {
                Map<String, Object> adminNotification = new HashMap<>();
                adminNotification.put("userId", "bawantha2819@gmail.com");
                adminNotification.put("userRole", "ADMIN");
                adminNotification.put("title", "Action Required: New Doctor Registration Waiting");
                String adminMessage = String.format(
                        "Dear Administrator,\n\n" +
                                "A new doctor registration request has been received and is awaiting your review.\n\n" +
                                "Doctor Details:\n" +
                                "- Name: Dr. %s %s\n" +
                                "- Specialty: %s\n" +
                                "- Registration Email: %s\n\n" +
                                "Please login to the Admin Dashboard to review the application and verify the credentials.\n\n"
                                +
                                "Regards,\n" +
                                "Medisphere System",
                        request.getFirstName(), request.getLastName(), request.getSpecialty(), request.getEmail());
                adminNotification.put("message", adminMessage);
                adminNotification.put("channel", "EMAIL");

                notificationClient.createNotification(adminNotification);
            } catch (Exception notificationEx) {
                System.err.println("Failed to send admin notification: " + notificationEx.getMessage());
            }

            return ApiResponse.<String>builder()
                    .status("SUCCESS")
                    .message("Doctor registration submitted for approval and Admin notified")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("Failed to submit doctor registration: " + e)
                    .build();
        }
    }

    @Transactional
    public ApiResponse<String> createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("User already exists in Auth Service")
                    .build();
        }

        User user = new User();
        user.setMsUserId(generateMsUserId(userDto.getRole()));
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword()); // Use pre-encoded password from Admin Service
        user.setRole(userDto.getRole());
        user.setCreatedDate(Instant.now());
        user.setModifiedDate(Instant.now());

        userRepository.save(user);

        // Sync with Doctor Service if the user is a DOCTOR
        if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            try {
                // Ensure all mandatory fields for Doctor Service have non-empty defaults
                CreateDoctorDTO createDoctorDTO = CreateDoctorDTO.builder()
                        .firstName(userDto.getFirstName() != null && !userDto.getFirstName().isEmpty()
                                ? userDto.getFirstName()
                                : "NA")
                        .lastName(userDto.getLastName() != null && !userDto.getLastName().isEmpty()
                                ? userDto.getLastName()
                                : "NA")
                        .doctorId(user.getMsUserId())
                        .msUserId(user.getMsUserId())
                        .specialty(userDto.getSpecialty() != null && !userDto.getSpecialty().isEmpty()
                                ? userDto.getSpecialty()
                                : "General")
                        .drLicence(userDto.getLicenseUrl() != null && !userDto.getLicenseUrl().isEmpty()
                                ? userDto.getLicenseUrl()
                                : "NA")
                        .drContactNo(
                                userDto.getPhone() != null && !userDto.getPhone().isEmpty() ? userDto.getPhone() : "NA")
                        .drNic(user.getMsUserId())
                        .status("ACTIVE")
                        .profilePic("N/A")
                        .createDate(Instant.now())
                        .modifiedDate(Instant.now())
                        .build();

                doctorClient.createDoctor(createDoctorDTO);
            } catch (Exception e) {
                // Throw exception to trigger rollback so we don't end up with zombie accounts
                throw new RuntimeException("User created, but failed to sync with Doctor Service: " + e.getMessage());
            }
        }

        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("User created successfully")
                .data(user.getMsUserId())
                .build();
    }

    public void validateToken(String token) {
        jwtUtil.validateTokenSimple(token);
    }

    @Transactional
    public ApiResponse<String> deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ApiResponse.<String>builder()
                    .status("SUCCESS")
                    .message("User not found or already deleted from Auth service")
                    .build();
        }

        // Sync deletion with respective service
        try {
            if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
                DeleteDoctorDTO deleteDto = new DeleteDoctorDTO(user.getMsUserId());
                doctorClient.deleteDoctor(user.getMsUserId(), deleteDto);
            } else if ("PATIENT".equalsIgnoreCase(user.getRole())) {
                patientClient.deletePatient(user.getMsUserId());
            }
        } catch (Exception e) {
            // Throw exception to trigger rollback in Auth service if sync fails
            throw new RuntimeException(
                    "Failed to synchronize deletion with " + user.getRole() + " service: " + e.getMessage());
        }

        userRepository.deleteByEmail(email);
        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("User and associated profile deleted successfully")
                .build();
    }

    @Transactional
    public ApiResponse<String> deleteUserByMsUserId(String msUserId) {
        userRepository.deleteByMsUserId(msUserId);
        return ApiResponse.<String>builder()
                .status("SUCCESS")
                .message("User deleted from Auth successfully via MS User ID")
                .build();
    }

    private String generateMsUserId(String role) {
        String prefix = role.equalsIgnoreCase("DOCTOR") ? "UD" : "UP";
        // Find the maximum numeric ID used so far for this specific role locally
        Long localMax = userRepository.findMaxIdByRole(role.toUpperCase()).orElse(0L);

        // Fetch maximum ID from remote service to prevent collisions
        Long remoteMax = 0L;
        try {
            if (role.equalsIgnoreCase("DOCTOR")) {
                remoteMax = doctorClient.getMaxMsUserId();
            } else {
                remoteMax = patientClient.getMaxMsUserId();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch remote max ID for " + role + ": " + e.getMessage());
        }

        Long finalMax = Math.max(localMax, remoteMax != null ? remoteMax : 0L);
        return String.format("%s%04d", prefix, finalMax + 1);
    }

    public ApiResponse<String> getEmailByMsUserId(String msUserId) {
        return userRepository.findByMsUserId(msUserId)
                .map(user -> ApiResponse.<String>builder()
                        .status("SUCCESS")
                        .message("Email found")
                        .data(user.getEmail())
                        .build())
                .orElse(ApiResponse.<String>builder()
                        .status("FAILED")
                        .message("User not found with ID: " + msUserId)
                        .build());
    }
}
