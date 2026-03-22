package com.clinic.system.service.impl;

import com.clinic.system.dto.request.LoginRequestDTO;
import com.clinic.system.dto.response.AuthResponseDTO;
import com.clinic.system.entity.User;
import com.clinic.system.exception.DuplicateResourceException;
import com.clinic.system.exception.ResourceNotFoundException;
import com.clinic.system.repository.UserRepository;
import com.clinic.system.security.JwtUtil;
import com.clinic.system.security.RefreshTokenService;
import com.clinic.system.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mail.com")
                .password("encodedPass")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ================= LOGIN =================

    @Test
    void login_success() {
        LoginRequestDTO dto = new LoginRequestDTO("testuser", "password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("testuser")).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("refreshToken");
        when(jwtUtil.getExpirationTime()).thenReturn(600000L);
        when(jwtUtil.getRefreshTokenExpirationTime()).thenReturn(1200000L);

        AuthResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("accessToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());

        verify(refreshTokenService).storeRefreshToken("testuser", "refreshToken");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_invalidCredentials() {
        LoginRequestDTO dto = new LoginRequestDTO("testuser", "wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(ResourceNotFoundException.class, () -> authService.login(dto));
    }

    // ================= REGISTER =================

    @Test
    void register_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        when(jwtUtil.generateToken("testuser")).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("refreshToken");
        when(jwtUtil.getExpirationTime()).thenReturn(600000L);
        when(jwtUtil.getRefreshTokenExpirationTime()).thenReturn(1200000L);

        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponseDTO response = authService.register(
                "testuser",
                "test@mail.com",
                "password",
                "Test User"
        );

        assertNotNull(response);
        assertEquals("accessToken", response.getToken());

        verify(refreshTokenService).storeRefreshToken("testuser", "refreshToken");
    }

    @Test
    void register_duplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                authService.register("testuser", "test@mail.com", "pass", "Test"));
    }

    @Test
    void register_duplicateEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                authService.register("testuser", "test@mail.com", "pass", "Test"));
    }

    // ================= LOGOUT =================

    @Test
    void logout_success() {
        String token = "jwtToken";

        when(jwtUtil.extractTokenId(token)).thenReturn("tokenId");
        when(jwtUtil.extractExpiration(token)).thenReturn(new Date());

        authService.logout("testuser", token);

        verify(tokenBlacklistService).blacklistToken(eq("tokenId"), anyLong());
        verify(refreshTokenService).revokeAllRefreshTokensForUser("testuser");
    }

    @Test
    void logout_exception() {
        String token = "jwtToken";

        when(jwtUtil.extractTokenId(token)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () ->
                authService.logout("testuser", token));
    }

    // ================= REFRESH TOKEN =================

    @Test
    void refreshToken_success() {
        String refreshToken = "refreshToken";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("testuser");
        when(jwtUtil.extractTokenId(refreshToken)).thenReturn("tokenId");

        when(refreshTokenService.isRefreshTokenValid("tokenId", "testuser"))
                .thenReturn(true);

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("testuser")).thenReturn("newAccessToken");
        when(jwtUtil.getExpirationTime()).thenReturn(600000L);
        when(jwtUtil.getRefreshTokenExpirationTime()).thenReturn(1200000L);

        AuthResponseDTO response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getToken());
    }

    @Test
    void refreshToken_invalidToken() {
        when(jwtUtil.validateToken("invalid")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> authService.refreshToken("invalid"));
    }

    @Test
    void refreshToken_notRefreshToken() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.isRefreshToken("token")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> authService.refreshToken("token"));
    }

    @Test
    void refreshToken_revokedToken() {
        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.isRefreshToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("testuser");
        when(jwtUtil.extractTokenId("token")).thenReturn("tokenId");

        when(refreshTokenService.isRefreshTokenValid("tokenId", "testuser"))
                .thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> authService.refreshToken("token"));
    }
}