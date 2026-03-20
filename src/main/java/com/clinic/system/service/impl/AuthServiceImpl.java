package com.clinic.system.service.impl;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.response.AuthResponseDTO;
import com.clinic.system.entity.User;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.UserRepository;
import com.clinic.system.security.JwtUtil;
import com.clinic.system.security.TokenBlacklistService;
import com.clinic.system.security.RefreshTokenService;
import com.clinic.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

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

            // Generate access token and refresh token
            String accessToken = jwtUtil.generateToken(username);
            String refreshToken = jwtUtil.generateRefreshToken(username);

            long expiresIn = jwtUtil.getExpirationTime();
            long refreshExpiresIn = jwtUtil.getRefreshTokenExpirationTime();

            // Store refresh token
            refreshTokenService.storeRefreshToken(username, refreshToken);

            log.info("User {} logged in successfully", username);

            return AuthResponseDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(accessToken)
                    .expiresIn(expiresIn)
                    .refreshToken(refreshToken)
                    .refreshExpiresIn(refreshExpiresIn)
                    .tokenType("Bearer")
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

        // Generate access token and refresh token
        String accessToken = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        long expiresIn = jwtUtil.getExpirationTime();
        long refreshExpiresIn = jwtUtil.getRefreshTokenExpirationTime();

        // Store refresh token
        refreshTokenService.storeRefreshToken(username, refreshToken);

        log.info("New user registered: {}", username);

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(accessToken)
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshExpiresIn)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(String username, String token) {
        try {
            // Extract token ID and blacklist it
            String tokenId = jwtUtil.extractTokenId(token);
            Date expirationDate = jwtUtil.extractExpiration(token);

            if (tokenId != null && expirationDate != null) {
                tokenBlacklistService.blacklistToken(tokenId, expirationDate.getTime());
            }

            // Revoke all refresh tokens for this user
            refreshTokenService.revokeAllRefreshTokensForUser(username);

            log.info("User {} logged out successfully. Token blacklisted.", username);
        } catch (Exception e) {
            log.error("Error during logout for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Logout failed", e);
        }
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshTokenStr) {
        try {
            // Validate refresh token
            if (!jwtUtil.validateToken(refreshTokenStr)) {
                throw new ResourceNotFoundException("Invalid or expired refresh token");
            }

            if (!jwtUtil.isRefreshToken(refreshTokenStr)) {
                throw new ResourceNotFoundException("Token is not a refresh token");
            }

            String username = jwtUtil.extractUsername(refreshTokenStr);
            String tokenId = jwtUtil.extractTokenId(refreshTokenStr);

            // Verify refresh token is stored and valid
            if (!refreshTokenService.isRefreshTokenValid(tokenId, username)) {
                throw new ResourceNotFoundException("Refresh token is revoked or invalid");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(username);
            long expiresIn = jwtUtil.getExpirationTime();

            log.info("Access token refreshed for user: {}", username);

            return AuthResponseDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(newAccessToken)
                    .expiresIn(expiresIn)
                    .refreshToken(refreshTokenStr)
                    .refreshExpiresIn(jwtUtil.getRefreshTokenExpirationTime())
                    .tokenType("Bearer")
                    .build();

        } catch (ResourceNotFoundException e) {
            log.warn("Refresh token failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed", e);
        }
    }
}
