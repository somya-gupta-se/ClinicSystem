package com.clinic.system.util;

/**
 * JWT Authentication - Complete Usage Examples
 *
 * This file contains detailed code examples for using the JWT authentication system.
 * Copy and adapt these examples for your client applications.
 */
public class JwtAuthenticationExamples {

    /**
     * EXAMPLE 1: Login Flow
     *
     * Step 1: Send login request
     * POST /auth/login
     * Content-Type: application/json
     *
     * {
     *   "usernameOrEmail": "admin",
     *   "password": "admin123"
     * }
     *
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "userId": 1,
     *     "username": "admin",
     *     "email": "admin@clinic.com",
     *     "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6IjEyMzQ1Njc4LWFiY2QtZWZnaC1pams",
     *     "expiresIn": 3600000,
     *     "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6IjEyMzQ1Njc4LWFiY2QtZWZnaC",
     *     "refreshExpiresIn": 604800000,
     *     "tokenType": "Bearer"
     *   },
     *   "message": "Login successful"
     * }
     *
     * IMPORTANT: Save the token and refreshToken for subsequent requests
     */
    public static class LoginExample {
        public static String LOGIN_ENDPOINT = "POST /api/auth/login";
        public static String REQUEST_BODY = """
                {
                  "usernameOrEmail": "admin",
                  "password": "admin123"
                }
                """;
        public static String TOKEN_EXPIRATION = "1 hour (3600000 ms)";
        public static String REFRESH_TOKEN_EXPIRATION = "7 days (604800000 ms)";
    }

    /**
     * EXAMPLE 2: Use Access Token for Protected Requests
     *
     * After login, use the access token for all protected API requests:
     *
     * GET /api/doctors
     * Authorization: Bearer {access_token}
     *
     * Example:
     * GET /api/doctors
     * Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6I...
     *
     * If token is valid:
     * - Request proceeds
     * - User is authenticated
     * - Response contains requested data
     *
     * If token is invalid/expired/blacklisted:
     * - Response: 403 Forbidden
     * - Error: "Access Denied"
     * - Action: Use refresh token to get new access token
     */
    public static class ProtectedRequestExample {
        public static String HEADER_NAME = "Authorization";
        public static String HEADER_VALUE = "Bearer {access_token}";
        public static String EXAMPLE_ENDPOINT = "GET /api/doctors";
    }

    /**
     * EXAMPLE 3: Refresh Access Token
     *
     * When access token expires (after 1 hour), use refresh token to get a new one.
     * This endpoint is PUBLIC (no authentication required).
     *
     * POST /auth/refresh
     * Content-Type: application/json
     *
     * {
     *   "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6I..."
     * }
     *
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "userId": 1,
     *     "username": "admin",
     *     "email": "admin@clinic.com",
     *     "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.{NEW_TOKEN_WITH_NEW_JTI}",
     *     "expiresIn": 3600000,
     *     "refreshToken": "{SAME_REFRESH_TOKEN}",
     *     "refreshExpiresIn": 604800000,
     *     "tokenType": "Bearer"
     *   },
     *   "message": "Access token refreshed successfully"
     * }
     *
     * IMPORTANT:
     * - The refresh token remains the same (don't need to store new one)
     * - The access token is NEW with a new jti claim
     * - Use the new access token for future requests
     * - Refresh token valid for 7 days
     */
    public static class RefreshTokenExample {
        public static String REFRESH_ENDPOINT = "POST /api/auth/refresh";
        public static String REQUEST_BODY = """
                {
                  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..."
                }
                """;
        public static String WHEN_TO_USE = "When access token expires (after 1 hour)";
        public static String RESPONSE_INCLUDES = "New access token with new jti, same refresh token";
    }

    /**
     * EXAMPLE 4: Logout Flow
     *
     * When user wants to end their session:
     *
     * POST /auth/logout
     * Authorization: Bearer {access_token}
     *
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": null,
     *   "message": "Logout successful. Session ended."
     * }
     *
     * WHAT HAPPENS WHEN YOU LOGOUT:
     * 1. Access token is blacklisted (cannot be used again)
     * 2. All refresh tokens are revoked
     * 3. SecurityContext is cleared on client
     * 4. Any subsequent requests with this token are rejected
     *
     * TO LOGIN AGAIN: Use the login endpoint with username/password
     */
    public static class LogoutExample {
        public static String LOGOUT_ENDPOINT = "POST /api/auth/logout";
        public static String REQUIRES_AUTHENTICATION = "YES (Bearer token required)";
        public static String REQUEST_BODY = "(empty)";
        public static String WHAT_GETS_BLACKLISTED = "Current access token jti claim";
        public static String WHAT_GETS_REVOKED = "All refresh tokens for the user";
    }

    /**
     * EXAMPLE 5: Complete Session Lifecycle
     *
     * TIME: 0:00 - User Logs In
     * curl -X POST http://localhost:8080/api/auth/login \
     *   -H "Content-Type: application/json" \
     *   -d '{"usernameOrEmail":"admin","password":"admin123"}'
     *
     * RESPONSE: accessToken + refreshToken
     * STORE: Both tokens in client storage
     *
     * ---
     * TIME: 0:05 - User Makes API Request
     * curl -X GET http://localhost:8080/api/doctors \
     *   -H "Authorization: Bearer {accessToken}"
     *
     * RESPONSE: 200 OK with doctor list
     * STATUS: Access token still valid
     *
     * ---
     * TIME: 1:05 - Access Token Expires (after 1 hour)
     * curl -X GET http://localhost:8080/api/doctors \
     *   -H "Authorization: Bearer {accessToken}"
     *
     * RESPONSE: 403 Forbidden (token expired)
     * ACTION: Use refresh token to get new access token
     *
     * ---
     * TIME: 1:05 - Refresh Access Token
     * curl -X POST http://localhost:8080/api/auth/refresh \
     *   -H "Content-Type: application/json" \
     *   -d '{"refreshToken":"{refreshToken}"}'
     *
     * RESPONSE: NEW accessToken + same refreshToken
     * STORE: Update accessToken, keep refreshToken
     *
     * ---
     * TIME: 2:00 - User Logs Out
     * curl -X POST http://localhost:8080/api/auth/logout \
     *   -H "Authorization: Bearer {newAccessToken}"
     *
     * RESPONSE: 200 OK - Logout successful. Session ended.
     * ACTION: Delete both tokens from client storage
     * RESULT: Cannot use either token anymore
     *
     * ---
     * TIME: 2:05 - Try to Use Old Token
     * curl -X GET http://localhost:8080/api/doctors \
     *   -H "Authorization: Bearer {oldAccessToken}"
     *
     * RESPONSE: 403 Forbidden (token is blacklisted)
     * ACTION: User must login again
     */
    public static class CompleteLifecycleExample {
        public static String SCENARIO = """
                User Session Lifecycle:
                Login (0:00) → Use Token (0:05) → Token Expires (1:05) → 
                Refresh Token (1:05) → Use New Token (1:10) → 
                Logout (2:00) → Old Token Rejected (2:05)
                """;
    }

    /**
     * EXAMPLE 6: Client Storage Strategy (JavaScript/Frontend)
     *
     * // After Login - Store tokens
     * localStorage.setItem('accessToken', response.data.token);
     * localStorage.setItem('refreshToken', response.data.refreshToken);
     * localStorage.setItem('tokenExpiration', Date.now() + response.data.expiresIn);
     *
     * // Before making API request - Check if token expired
     * const accessToken = localStorage.getItem('accessToken');
     * const tokenExpiration = parseInt(localStorage.getItem('tokenExpiration'));
     *
     * if (Date.now() > tokenExpiration) {
     *   // Token expired, refresh it
     *   const refreshToken = localStorage.getItem('refreshToken');
     *   const newTokenResponse = await fetch('/api/auth/refresh', {
     *     method: 'POST',
     *     headers: { 'Content-Type': 'application/json' },
     *     body: JSON.stringify({ refreshToken })
     *   });
     *
     *   const newData = await newTokenResponse.json();
     *   localStorage.setItem('accessToken', newData.data.token);
     *   localStorage.setItem('tokenExpiration', Date.now() + newData.data.expiresIn);
     * }
     *
     * // Make API request with current token
     * const currentToken = localStorage.getItem('accessToken');
     * fetch('/api/doctors', {
     *   headers: { 'Authorization': `Bearer ${currentToken}` }
     * });
     *
     * // On Logout
     * localStorage.removeItem('accessToken');
     * localStorage.removeItem('refreshToken');
     * localStorage.removeItem('tokenExpiration');
     */
    public static class ClientImplementationExample {
        public static String STORAGE_STRATEGY = "localStorage or sessionStorage";
        public static String STORE_ACCESS_TOKEN = "YES";
        public static String STORE_REFRESH_TOKEN = "YES";
        public static String AUTO_REFRESH = "Check expiration before each request";
    }

    /**
     * EXAMPLE 7: Error Responses
     *
     * INVALID CREDENTIALS:
     * POST /auth/login with wrong password
     * Response (404 Not Found):
     * {
     *   "success": false,
     *   "data": null,
     *   "message": "Invalid username/email or password"
     * }
     *
     * EXPIRED REFRESH TOKEN:
     * POST /auth/refresh with old/expired refresh token
     * Response (404 Not Found):
     * {
     *   "success": false,
     *   "data": null,
     *   "message": "Invalid or expired refresh token"
     * }
     *
     * BLACKLISTED ACCESS TOKEN (after logout):
     * GET /api/doctors with blacklisted token
     * Response (403 Forbidden):
     * (Request is rejected by JwtFilter before reaching endpoint)
     *
     * MISSING AUTHORIZATION HEADER:
     * GET /api/doctors without Authorization header
     * Response (403 Forbidden):
     * (Spring Security rejects unauthenticated request)
     *
     * MALFORMED TOKEN:
     * GET /api/doctors with invalid token format
     * Response (403 Forbidden):
     * (JwtFilter validation fails)
     */
    public static class ErrorResponseExample {
        public static String INVALID_CREDENTIALS = "404 Not Found";
        public static String EXPIRED_REFRESH = "404 Not Found or Refresh fails";
        public static String BLACKLISTED_TOKEN = "403 Forbidden";
        public static String MISSING_AUTH = "403 Forbidden";
    }

    /**
     * TOKEN STRUCTURE - What's Inside JWT
     *
     * JWT Format: {header}.{payload}.{signature}
     *
     * HEADER:
     * {
     *   "alg": "HS512",
     *   "typ": "JWT"
     * }
     *
     * PAYLOAD (Access Token):
     * {
     *   "sub": "admin",           // username
     *   "jti": "uuid-here",       // unique token ID (for blacklist tracking)
     *   "type": "access",         // token type
     *   "iat": 1647864000,        // issued at (unix timestamp)
     *   "exp": 1647867600         // expires at (unix timestamp, +1 hour)
     * }
     *
     * PAYLOAD (Refresh Token):
     * {
     *   "sub": "admin",
     *   "jti": "different-uuid",  // different ID than access token
     *   "type": "refresh",        // token type
     *   "iat": 1647864000,
     *   "exp": 1648469400         // expires after 7 days
     * }
     *
     * SIGNATURE:
     * - HMAC-SHA512 using jwt.secret from application.properties
     * - Ensures token has not been tampered with
     * - Only server can create/verify with the secret
     */
    public static class TokenStructureExample {
        public static String TOKEN_TYPE = "JWT (JSON Web Token)";
        public static String ALGORITHM = "HS512 (HMAC-SHA512)";
        public static String SIGNATURE_SECRET = "jwt.secret from application.properties";
    }
}

