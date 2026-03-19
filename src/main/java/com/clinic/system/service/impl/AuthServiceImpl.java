package com.clinic.system.service.impl;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.response.AuthResponseDTO;
import com.clinic.system.entity.User;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.UserRepository;
import com.clinic.system.security.JwtFilter;
import com.clinic.system.security.JwtUtil;
import com.clinic.system.service.AuthService;
import com.clinic.system.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;



    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.usernameOrEmail(),
                            dto.password()
                    )
            );

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> userRepository.findByEmail(username)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found")));

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(username);
            long expiresIn = jwtUtil.getExpirationTime();

            log.info("User {} logged in successfully", username);

            return AuthResponseDTO.builder()
                    .token(token)
                    .expiresIn(expiresIn)
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", dto.usernameOrEmail());
            throw new ResourceNotFoundException("Invalid username/email or password");
        }
    }

    @Override
    public AuthResponseDTO register(String username, String email, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(username);
        long expiresIn = jwtUtil.getExpirationTime();

        log.info("New user registered: {}", username);

        return AuthResponseDTO.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }

    @Override
    public void logout(String username) {
        log.info("User {} logged out", username);
        // In a stateless JWT system, logout is typically handled client-side by deleting the token
        // However, if token blacklisting is needed, implement a token blacklist mechanism
    }
}

