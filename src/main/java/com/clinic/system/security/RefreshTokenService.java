package com.clinic.system.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final ConcurrentHashMap<String, RefreshTokenData> refreshTokenStore = new ConcurrentHashMap<>();
    private final JwtUtil jwtUtil;

    public void storeRefreshToken(String username, String refreshToken) {
        if (username == null || refreshToken == null) {
            return;
        }

        String tokenId = jwtUtil.extractTokenId(refreshToken);
        long expirationTime = jwtUtil.extractExpiration(refreshToken).getTime();

        RefreshTokenData tokenData = RefreshTokenData.builder()
                .username(username)
                .tokenId(tokenId)
                .token(refreshToken)
                .expirationTime(expirationTime)
                .createdAt(System.currentTimeMillis())
                .build();

        refreshTokenStore.put(tokenId, tokenData);
        log.info("Refresh token stored for user: {}", username);
    }

    public boolean isRefreshTokenValid(String tokenId, String username) {
        if (tokenId == null || username == null) {
            return false;
        }

        RefreshTokenData tokenData = refreshTokenStore.get(tokenId);
        if (tokenData == null) {
            log.warn("Refresh token not found for ID: {}", tokenId);
            return false;
        }

        long currentTime = System.currentTimeMillis();
        boolean isExpired = tokenData.getExpirationTime() < currentTime;

        if (isExpired) {
            log.warn("Refresh token expired for user: {}", username);
            refreshTokenStore.remove(tokenId);
            return false;
        }

        boolean isUsernameMatch = tokenData.getUsername().equals(username);
        if (!isUsernameMatch) {
            log.warn("Username mismatch for refresh token: {} vs {}", tokenData.getUsername(), username);
        }

        return isUsernameMatch;
    }

    public void revokeRefreshToken(String tokenId) {
        if (tokenId != null) {
            refreshTokenStore.remove(tokenId);
            log.info("Refresh token revoked: {}", tokenId);
        }
    }

    public void revokeAllRefreshTokensForUser(String username) {
        if (username != null) {
            refreshTokenStore.entrySet().removeIf(entry ->
                entry.getValue().getUsername().equals(username));
            log.info("All refresh tokens revoked for user: {}", username);
        }
    }

    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int sizeBefore = refreshTokenStore.size();

        refreshTokenStore.entrySet().removeIf(entry ->
            entry.getValue().getExpirationTime() < currentTime);

        int sizeAfter = refreshTokenStore.size();
        if (sizeBefore != sizeAfter) {
            log.info("Cleaned up {} expired refresh tokens. Remaining: {}",
                    sizeBefore - sizeAfter, sizeAfter);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class RefreshTokenData {
        private String username;
        private String tokenId;
        private String token;
        private long expirationTime;
        private long createdAt;
    }
}

