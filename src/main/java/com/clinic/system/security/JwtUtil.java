package com.clinic.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:MyVerySecureSecretKeyFor256BitHSAlgorithmThatIsAtLeast32CharactersLong}")
    private String jwtSecret;

    @Value("${jwt.expiration:300000}")
    private long jwtExpiration; // 5 mins in milliseconds

    @Value("${jwt.refresh-expiration:600000}")
    private long jwtRefreshExpiration; // 10 mins in milliseconds


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String tokenId = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claim("jti", tokenId)
                .claim("type", "access")
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String tokenId = UUID.randomUUID().toString();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .claim("jti", tokenId)
                .claim("type", "refresh")
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token is null or empty");
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.error("Error extracting claim from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Error parsing JWT claims: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error parsing JWT: {}", e.getMessage());
            throw new JwtException("Error parsing JWT", e);
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            if (expiration == null) {
                log.warn("Token expiration date is null");
                return true;
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't determine
        }
    }

    public Boolean isTokenValid(String token, String username) {
        if (token == null || token.trim().isEmpty() || username == null || username.trim().isEmpty()) {
            log.warn("Token or username is null/empty for validation");
            return false;
        }

        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public String extractTokenId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            return extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (Exception e) {
            log.error("Error extracting token ID: {}", e.getMessage());
            return null;
        }
    }

    public String extractTokenType(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            return extractClaim(token, claims -> claims.get("type", String.class));
        } catch (Exception e) {
            log.error("Error extracting token type: {}", e.getMessage());
            return null;
        }
    }

    public Boolean isAccessToken(String token) {
        String type = extractTokenType(token);
        return "access".equals(type);
    }

    public Boolean isRefreshToken(String token) {
        String type = extractTokenType(token);
        return "refresh".equals(type);
    }

    public long getRefreshTokenExpirationTime() {
        return jwtRefreshExpiration;
    }
}

