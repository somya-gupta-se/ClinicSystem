package com.clinic.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private String token;
    private long expiresIn;
    private String refreshToken;
    private long refreshExpiresIn;
    private String tokenType;
}