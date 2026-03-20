package com.clinic.system.controller;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.request.RefreshTokenRequestDTO;
import com.clinic.system.dto.request.RegisterRequestDTO;
import com.clinic.system.dto.response.ApiResponseDTO;
import com.clinic.system.dto.response.AuthResponseDTO;
import com.clinic.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<ApiResponseDTO<Void>> logout(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Extract access token from Authorization header for blacklisting
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Logout and blacklist the token
        authService.logout(username, token);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponseDTO.success(null, "Logout successful. Session ended."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<AuthResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO dto) {
        AuthResponseDTO response = authService.refreshToken(dto.refreshToken());
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Access token refreshed successfully"));
    }
}
