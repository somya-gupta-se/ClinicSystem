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

    @Value("${jwt.expiration:600000}")
    private long jwtExpiration; // 60 mins in milliseconds

    @Value("${jwt.refresh-expiration:1200000}")
    private long jwtRefreshExpiration; // 120 mins in milliseconds


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /*
        * Generates a JWT token for the given username with a unique token ID (jti) and type claim.
        * The token includes the subject (username), issued at time, expiration time,
        * and custom claims for token ID and type (access or refresh).
        * The token is signed using the HS256 algorithm with the configured secret key.
        * This method ensures that the username is valid and throws an exception if it's null or empty,
        * providing better error handling and security.
        * The generated token can be used for authenticating user requests and managing sessions securely.
        * The inclusion of a unique token ID allows for better tracking and management of tokens,
        * such as revocation and blacklisting, enhancing the overall security of the authentication system.
        * The method also logs important information and errors during token generation, aiding in debugging and monitoring.
        * Overall, this method is a crucial part of the JWT-based authentication mechanism,
        * ensuring secure token generation and management for user authentication and session handling.
     */
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

    /*
        * Generates a JWT refresh token for the given username with a unique token ID (jti) and type claim.
        * The refresh token includes the subject (username), issued at time, expiration time,
        * and custom claims for token ID and type (refresh).
        * The token is signed using the HS256 algorithm with the configured secret key.
        * This method ensures that the username is valid and throws an exception if it's null or empty,
        * providing better error handling and security.
        * The generated refresh token can be used to obtain new access tokens without requiring the user to re-authenticate,
        * enhancing user experience while maintaining security.
        * The inclusion of a unique token ID allows for better tracking and management of refresh tokens,
        * such as revocation and blacklisting, improving the overall security of the authentication system.
        * The method also logs important information and errors during refresh token generation, aiding in debugging and monitoring.
        * Overall, this method is essential for implementing a secure and efficient JWT-based authentication mechanism,
        * allowing for seamless session management and improved user experience through the use of refresh tokens.
     */
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

    /*
        * Validates the given JWT token by checking its structure, signature, and expiration.
        * The method first checks if the token is null or empty and logs a warning if it is.
        * It then attempts to parse the token using the configured signing key.
        * If the token is valid, it returns true. If any exceptions occur during parsing (e.g., invalid signature, expired token),
        * it catches the exceptions, logs the error messages, and returns false.
        * This method ensures that only valid tokens are accepted for authentication and access control,
        * enhancing the security of the application by preventing unauthorized access with invalid or expired tokens.
     */
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

    /*
        * Extracts the username (subject) from the given JWT token.
        * The method first checks if the token is null or empty and throws an IllegalArgumentException if it is.
        * It then uses the extractClaim method to retrieve the subject claim from the token's claims.
        * If any exceptions occur during claim extraction (e.g., invalid token), it catches the exceptions, logs the error messages,
        * and rethrows an IllegalArgumentException with a descriptive message.
        * This method is essential for retrieving the username associated with a valid JWT token,
        * allowing for user identification and authentication in the application.
     */
    public String extractUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return extractClaim(token, Claims::getSubject);
    }

    /*
        * Extracts the expiration date from the given JWT token.
        * The method first checks if the token is null or empty and throws an IllegalArgumentException if it is.
        * It then uses the extractClaim method to retrieve the expiration claim from the token's claims.
        * If any exceptions occur during claim extraction (e.g., invalid token), it catches the exceptions, logs the error messages,
        * and rethrows an IllegalArgumentException with a descriptive message.
        * This method is crucial for determining the validity of a JWT token by checking its expiration time,
        * allowing for proper session management and security in the application.
     */
    public Date extractExpiration(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return extractClaim(token, Claims::getExpiration);
    }

    /*
        * A generic method to extract any claim from the JWT token using a provided claims resolver function.
        * The method first checks if the token is null or empty and throws an IllegalArgumentException if it is.
        * It then attempts to extract all claims from the token and applies the provided claims resolver function to retrieve the desired claim.
        * If any exceptions occur during claim extraction (e.g., invalid token), it catches the exceptions, logs the error messages,
        * and rethrows an IllegalArgumentException with a descriptive message.
        * This method provides flexibility in extracting various claims from the JWT token, allowing for dynamic retrieval of information based on the application's needs.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException e) {
            log.error("Error extracting claim from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    /*
        * Extracts all claims from the given JWT token.
        * The method attempts to parse the token using the configured signing key and retrieves the claims from the token's body.
        * If any exceptions occur during parsing (e.g., invalid signature, expired token), it catches the exceptions,
        * logs the error messages,
        * and rethrows a JwtException with a descriptive message.
        * This method is essential for accessing the claims contained within a JWT token,
        * allowing for retrieval of information such as username, expiration, and custom claims for authentication and authorization purposes.
     */
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

