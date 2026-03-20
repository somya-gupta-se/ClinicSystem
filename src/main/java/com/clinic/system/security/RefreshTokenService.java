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

    /*
        * Stores the refresh token associated with the username.
        * Extracts the token ID and expiration time from the refresh token and saves it in the in-memory store.
        * If either the username or refresh token is null, the method will simply return without storing anything.
        * This method is crucial for managing refresh tokens, allowing the application to validate and revoke them as needed.
        * The refresh token data includes the username, token ID, the token itself, expiration time, and creation time, which can be used for future validation and cleanup of expired tokens.
        * Note: In a production environment, consider using a persistent storage solution (like Redis or a database) instead of an in-memory store to handle refresh tokens, especially in a distributed system.
        * This implementation is suitable for demonstration purposes or single-instance applications but may not be ideal for production use due to potential memory issues and lack of persistence across application restarts.
        * In a real-world application, you would also want to implement additional security measures, such as encrypting the refresh tokens in storage and implementing a more robust cleanup strategy for expired tokens.
        * Overall, this method is a key part of the refresh token management process, ensuring that refresh tokens are properly stored and can be validated or revoked as needed to maintain the security of the authentication system.
        * @param username the username associated with the refresh token
        * @param refreshToken the refresh token to be stored
     */
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

