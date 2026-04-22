package com.medisphere.auth.controller;

import com.medisphere.auth.dto.*;
import com.medisphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<ApiResponse<String>> registerPatient(@RequestBody RegisterPatientRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<ApiResponse<String>> registerDoctor(@RequestBody RegisterDoctorRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<String>> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(authService.createUser(userDto));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam("token") String token) {
        try {
            authService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("SUCCESS")
                    .message("Token is valid")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.<String>builder()
                    .status("FAILED")
                    .message("Invalid token: " + e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestParam String email) {
        return ResponseEntity.ok(authService.deleteUser(email));
    }

    @GetMapping("/email/{msUserId}")
    public ResponseEntity<ApiResponse<String>> getEmailByMsUserId(@PathVariable("msUserId") String msUserId) {
        return ResponseEntity.ok(authService.getEmailByMsUserId(msUserId));
    }
}
