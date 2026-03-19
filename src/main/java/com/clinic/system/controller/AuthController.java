package com.clinic.system.controller;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.request.RegisterRequestDTO;
import com.clinic.system.dto.response.ApiResponseDTO;
import com.clinic.system.dto.response.AuthResponseDTO;
import com.clinic.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;




    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO dto) {
        AuthResponseDTO response = authService.register(dto.username(), dto.email(), dto.password(), dto.fullName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(response, "Registration successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(username);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Logout successful"));
    }
}
