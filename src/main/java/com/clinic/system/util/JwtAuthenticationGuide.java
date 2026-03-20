package com.clinic.system.util;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT Authentication & Session Management Implementation Guide
 *
 * This document provides detailed information about the JWT-based Login/Logout
 * implementation with active session management using refresh tokens.
 *
 * ========================================
 * IMPLEMENTATION SUMMARY
 * ========================================
 *
 * 1. SESSION DURATION: 1 hour (3600000 milliseconds)
 * 2. REFRESH TOKEN EXPIRATION: 7 days (604800000 milliseconds)
 * 3. TOKEN TYPE: JWT with jti (JWT ID) claim for tracking
 * 4. TOKEN BLACKLIST: In-memory ConcurrentHashMap with scheduled cleanup
 * 5. REFRESH TOKEN STORAGE: In-memory ConcurrentHashMap
 *
 * ========================================
 * AUTHENTICATION FLOW
 * ========================================
 *
 * LOGIN (POST /auth/login)
 * ├── Request: LoginRequestDTO { usernameOrEmail, password }
 * ├── Response: AuthResponseDTO {
 * │   userId, username, email,
 * │   token (access token), expiresIn (1 hour),
 * │   refreshToken, refreshExpiresIn (7 days),
 * │   tokenType: "Bearer"
 * │ }
 * ├── User authentication via Spring Security
 * ├── Access token generated with jti claim
 * ├── Refresh token generated and stored
 * └── Last login timestamp updated
 *
 * PROTECTED REQUESTS (with access token)
 * ├── Header: Authorization: Bearer {access_token}
 * ├── JwtFilter checks token blacklist
 * ├── JwtFilter validates token signature and expiration
 * ├── If valid: User authenticated and request proceeds
 * └── If invalid/expired: Request denied with 403
 *
 * REFRESH TOKEN (POST /auth/refresh)
 * ├── Request: RefreshTokenRequestDTO { refreshToken }
 * ├── Response: AuthResponseDTO { new token, same refreshToken, ... }
 * ├── Validate refresh token is valid and not revoked
 * ├── Generate new access token with new jti claim
 * └── Return new access token (refresh token remains valid)
 *
 * LOGOUT (POST /auth/logout)
 * ├── Request: (requires Bearer token in Authorization header)
 * ├── Response: ApiResponseDTO { message: "Logout successful. Session ended." }
 * ├── Extract access token jti and blacklist it
 * ├── Revoke all refresh tokens for the user
 * ├── Clear SecurityContext
 * └── Token cannot be used for future requests
 *
 * ========================================
 * KEY COMPONENTS
 * ========================================
 *
 * 1. JwtUtil.java
 *    - generateToken(username): Creates access token with jti claim
 *    - generateRefreshToken(username): Creates refresh token with jti claim
 *    - validateToken(token): Validates token signature and expiration
 *    - extractTokenId(token): Extracts jti claim (unique token ID)
 *    - extractTokenType(token): Extracts token type (access/refresh)
 *    - isAccessToken(token): Checks if token is access token
 *    - isRefreshToken(token): Checks if token is refresh token
 *
 * 2. TokenBlacklistService.java (Security/Session Management)
 *    - blacklistToken(tokenId, expirationTime): Adds token to blacklist
 *    - isTokenBlacklisted(tokenId): Checks if token is blacklisted
 *    - removeTokenFromBlacklist(tokenId): Removes token from blacklist
 *    - cleanupExpiredTokens(): Scheduled cleanup every 1 hour
 *      (Prevents memory issues by removing expired entries)
 *
 * 3. RefreshTokenService.java
 *    - storeRefreshToken(username, refreshToken): Stores in-memory
 *    - isRefreshTokenValid(tokenId, username): Validates stored token
 *    - revokeRefreshToken(tokenId): Revokes single token
 *    - revokeAllRefreshTokensForUser(username): Logout all devices
 *    - cleanupExpiredTokens(): Cleanup expired tokens
 *
 * 4. JwtFilter.java
 *    - Checks token blacklist BEFORE validation (early exit)
 *    - Extracts token from Authorization header
 *    - Validates token if not blacklisted
 *    - Sets SecurityContext with authenticated user
 *
 * 5. AuthServiceImpl.java
 *    - login(): Generates both access & refresh tokens
 *    - register(): Creates user and generates tokens
 *    - logout(username, token): Blacklists token and revokes refresh tokens
 *    - refreshToken(refreshToken): Generates new access token
 *
 * ========================================
 * API ENDPOINTS
 * ========================================
 *
 * POST /auth/login
 *   Body: { "usernameOrEmail": "admin", "password": "admin123" }
 *   Returns: Access token, refresh token, and user details
 *   Status: 200 OK
 *
 * POST /auth/register
 *   Body: {
 *     "username": "newuser",
 *     "email": "user@example.com",
 *     "password": "password123",
 *     "fullName": "New User"
 *   }
 *   Returns: Access token, refresh token, and user details
 *   Status: 201 CREATED
 *
 * POST /auth/refresh
 *   Body: { "refreshToken": "eyJhbGciOiJIUzUxMiJ9..." }
 *   Returns: New access token with same refresh token
 *   Status: 200 OK
 *   Note: No authentication required (public endpoint)
 *
 * POST /auth/logout
 *   Header: Authorization: Bearer {access_token}
 *   Body: (empty)
 *   Returns: { "message": "Logout successful. Session ended." }
 *   Status: 200 OK
 *   Note: Token blacklisted, cannot be reused
 *
 * ========================================
 * SECURITY FEATURES
 * ========================================
 *
 * 1. TOKEN BLACKLISTING
 *    - When user logs out, their access token is blacklisted
 *    - Blacklisted tokens cannot be used for API requests
 *    - Stored tokens are cleaned up after expiration
 *
 * 2. REFRESH TOKEN MANAGEMENT
 *    - Refresh tokens are stored server-side
 *    - User can log out from all devices (revoke all tokens)
 *    - Tokens are revoked on logout
 *
 * 3. JWT CLAIMS
 *    - "sub" (subject): username
 *    - "jti" (JWT ID): unique token identifier for tracking
 *    - "type": token type (access/refresh)
 *    - "iat" (issued at): token creation time
 *    - "exp" (expiration): token expiration time
 *
 * 4. STATELESS AUTHENTICATION
 *    - JWT signature validation ensures token authenticity
 *    - No server session storage needed (except blacklist/refresh tokens)
 *    - Scalable across multiple servers
 *
 * 5. PASSWORD SECURITY
 *    - Passwords are hashed using BCrypt
 *    - Never transmitted in plain text
 *    - Spring Security handles authentication
 *
 * ========================================
 * CURL EXAMPLES
 * ========================================
 *
 * 1. LOGIN
 * curl -X POST http://localhost:8080/api/auth/login \
 *   -H "Content-Type: application/json" \
 *   -d '{"usernameOrEmail":"admin","password":"admin123"}'
 *
 * Response:
 * {
 *   "success": true,
 *   "data": {
 *     "userId": 1,
 *     "username": "admin",
 *     "email": "admin@clinic.com",
 *     "token": "eyJhbGciOiJIUzUxMiJ9...",
 *     "expiresIn": 3600000,
 *     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
 *     "refreshExpiresIn": 604800000,
 *     "tokenType": "Bearer"
 *   },
 *   "message": "Login successful"
 * }
 *
 * 2. MAKE AUTHENTICATED REQUEST
 * curl -X GET http://localhost:8080/api/doctors \
 *   -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
 *
 * 3. REFRESH TOKEN
 * curl -X POST http://localhost:8080/api/auth/refresh \
 *   -H "Content-Type: application/json" \
 *   -d '{"refreshToken":"eyJhbGciOiJIUzUxMiJ9..."}'
 *
 * 4. LOGOUT
 * curl -X POST http://localhost:8080/api/auth/logout \
 *   -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
 *
 * ========================================
 * CONFIGURATION (application.properties)
 * ========================================
 *
 * jwt.secret=MyVerySecureSecretKeyFor256BitHSAlgorithmThatIsAtLeast32CharactersLongForProduction
 * jwt.expiration=3600000          (1 hour in milliseconds)
 * jwt.refresh-expiration=604800000 (7 days in milliseconds)
 *
 * ========================================
 * TOKEN EXPIRATION BEHAVIOR
 * ========================================
 *
 * SCENARIO 1: Access Token Expires (after 1 hour)
 * - User makes request with expired access token
 * - JwtFilter validation fails (token expired)
 * - Client receives 403 Forbidden
 * - Client uses refresh token to get new access token
 *
 * SCENARIO 2: Refresh Token Expires (after 7 days)
 * - User tries to refresh with expired refresh token
 * - Refresh endpoint returns 404 (token not found)
 * - User must login again
 *
 * SCENARIO 3: User Logs Out
 * - Access token is blacklisted immediately
 * - All refresh tokens are revoked
 * - Token cannot be used for any request
 *
 * ========================================
 * PRODUCTION CONSIDERATIONS
 * ========================================
 *
 * 1. SECURITY:
 *    - Use HTTPS in production (JWT in Authorization header)
 *    - Increase JWT secret length (currently 64+ chars)
 *    - Consider setting HttpOnly flag on cookies (if used)
 *
 * 2. PERFORMANCE:
 *    - Token blacklist uses in-memory ConcurrentHashMap
 *    - For distributed systems, use Redis instead
 *    - Refresh token store uses in-memory ConcurrentHashMap
 *    - For distributed systems, use database persistence
 *
 * 3. SCALABILITY:
 *    - Current implementation works for single server
 *    - For multi-server setup:
 *      a) Store blacklist in Redis
 *      b) Store refresh tokens in database or Redis
 *      c) Use shared JWT secret across servers
 *
 * 4. MONITORING:
 *    - Log all login/logout events
 *    - Monitor token refresh failures
 *    - Track authentication failures
 *
 * ========================================
 * IMPLEMENTATION STATUS: ✅ COMPLETE
 * ========================================
 *
 * ✅ JWT Token Generation (1-hour access, 7-day refresh)
 * ✅ Token Blacklisting (on logout)
 * ✅ Refresh Token Management
 * ✅ JwtFilter Integration
 * ✅ Token Type Tracking (jti claim)
 * ✅ Active Session Maintenance
 * ✅ Login Endpoint
 * ✅ Logout Endpoint
 * ✅ Refresh Endpoint
 * ✅ Register Endpoint
 * ✅ Security Configuration
 * ✅ Password Encoding (BCrypt)
 * ✅ Last Login Tracking
 * ✅ Error Handling
 *
 */
@RestController
@RequestMapping("/auth-docs")
@Tag(name = "JWT Authentication", description = "JWT authentication implementation guide")
public class JwtAuthenticationGuide {
    // This is a documentation class
    // See comments above for complete implementation details
}

