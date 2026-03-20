package com.clinic.system.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String tokenId, long expirationTime) {
        if (tokenId != null && !tokenId.trim().isEmpty()) {
            blacklistedTokens.put(tokenId, expirationTime);
            log.info("Token ID {} has been blacklisted", tokenId);
        }
    }

    public boolean isTokenBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return false;
        }
        return blacklistedTokens.containsKey(tokenId);
    }

    public void removeTokenFromBlacklist(String tokenId) {
        if (tokenId != null && !tokenId.trim().isEmpty()) {
            blacklistedTokens.remove(tokenId);
            log.info("Token ID {} has been removed from blacklist", tokenId);
        }
    }

    public void clearBlacklist() {
        blacklistedTokens.clear();
        log.info("Token blacklist has been cleared");
    }

    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    /**
     * Cleanup expired tokens from blacklist every hour.
     * This prevents the blacklist map from growing indefinitely.
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int sizeBefore = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);

        int sizeAfter = blacklistedTokens.size();
        if (sizeBefore != sizeAfter) {
            log.info("Cleaned up {} expired tokens from blacklist. Remaining: {}",
                    sizeBefore - sizeAfter, sizeAfter);
        }
    }
}

