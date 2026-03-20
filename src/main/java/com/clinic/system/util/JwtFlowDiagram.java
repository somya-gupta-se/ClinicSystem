package com.clinic.system.util;

/**
 * COMPLETE JWT AUTHENTICATION FLOW DIAGRAM
 *
 * This document shows the detailed flow of JWT-based authentication with
 * token refresh and logout/blacklisting.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 1. LOGIN FLOW (Initial Authentication)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT                                      SERVER
 *
 * POST /auth/login
 * {
 *   "usernameOrEmail": "admin",
 *   "password": "admin123"
 * }
 * ────────────────────────────────────────→  AuthController.login()
 *                                              │
 *                                              ├→ JwtFilter: No authentication needed
 *                                              │
 *                                              └→ AuthServiceImpl.login()
 *                                                  │
 *                                                  ├→ authenticationManager.authenticate()
 *                                                  │    └─ BCrypt password comparison
 *                                                  │
 *                                                  ├→ userRepository.findByUsername()
 *                                                  │    └─ Get user from database
 *                                                  │
 *                                                  ├→ Update user.lastLogin = NOW()
 *                                                  │
 *                                                  ├→ JwtUtil.generateToken(username)
 *                                                  │    └─ Create access token with:
 *                                                  │       • sub: "admin"
 *                                                  │       • jti: "unique-uuid-1"
 *                                                  │       • type: "access"
 *                                                  │       • exp: now + 1 hour
 *                                                  │
 *                                                  ├→ JwtUtil.generateRefreshToken(username)
 *                                                  │    └─ Create refresh token with:
 *                                                  │       • sub: "admin"
 *                                                  │       • jti: "unique-uuid-2"
 *                                                  │       • type: "refresh"
 *                                                  │       • exp: now + 7 days
 *                                                  │
 *                                                  └→ RefreshTokenService.storeRefreshToken()
 *                                                       └─ Store in ConcurrentHashMap:
 *                                                          {
 *                                                            "unique-uuid-2": {
 *                                                              username: "admin",
 *                                                              expirationTime: ...,
 *                                                              ...
 *                                                            }
 *                                                          }
 * ←────────────────────────────────────────  AuthResponseDTO
 * SAVE TOKENS:                              {
 *   accessToken = "..."                       userId: 1,
 *   refreshToken = "..."                      username: "admin",
 *   tokenExpiration = now + 1 hour            token: "...",
 *   refreshExpiration = now + 7 days          refreshToken: "...",
 * }                                           expiresIn: 3600000,
 *                                               refreshExpiresIn: 604800000,
 *                                               tokenType: "Bearer"
 *                                             }
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 2. PROTECTED REQUEST FLOW (Using Access Token)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT                                      SERVER
 *
 * GET /api/doctors
 * Authorization: Bearer {accessToken}
 * ────────────────────────────────────────→  Spring DispatcherServlet
 *                                              │
 *                                              ├→ JwtFilter.doFilterInternal()
 *                                              │    │
 *                                              │    ├→ Extract header: "Authorization: Bearer ..."
 *                                              │    │
 *                                              │    ├→ Extract token from "Bearer {token}"
 *                                              │    │
 *                                              │    ├→ JwtUtil.extractTokenId(token)
 *                                              │    │    └─ Extract jti: "unique-uuid-1"
 *                                              │    │
 *                                              │    ├→ TokenBlacklistService.isTokenBlacklisted()
 *                                              │    │    └─ Check if "unique-uuid-1" in blacklist
 *                                              │    │       └─ Return: false (not blacklisted)
 *                                              │    │
 *                                              │    ├→ JwtUtil.validateToken(token)
 *                                              │    │    │
 *                                              │    │    ├→ Verify signature (HS512)
 *                                              │    │    │    └─ Uses jwt.secret from properties
 *                                              │    │    │
 *                                              │    │    ├→ Check expiration
 *                                              │    │    │    └─ Extract exp: 1647867600
 *                                              │    │    │
 *                                              │    │    └─ Return: true (valid)
 *                                              │    │
 *                                              │    ├→ JwtUtil.extractUsername(token)
 *                                              │    │    └─ Extract sub: "admin"
 *                                              │    │
 *                                              │    ├→ CustomUserDetailsService.loadUserByUsername("admin")
 *                                              │    │    └─ Get UserDetails from database
 *                                              │    │
 *                                              │    └→ Set SecurityContext
 *                                              │         {
 *                                              │           authentication: {
 *                                              │             principal: UserDetails("admin"),
 *                                              │             authorities: [],
 *                                              │             authenticated: true
 *                                              │           }
 *                                              │         }
 *                                              │
 *                                              ├→ DoctorController.getAllDoctors()
 *                                              │    ├→ Check @PreAuthorize (if any)
 *                                              │    ├→ Load doctors from database
 *                                              │    └→ Return: List<Doctor>
 *                                              │
 * ←────────────────────────────────────────  200 OK
 * DISPLAY DOCTORS                           [
 *                                             { id: 1, name: "Dr. Smith", ... },
 *                                             { id: 2, name: "Dr. Jones", ... }
 *                                           ]
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 3. TOKEN REFRESH FLOW (When Access Token Expires)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Scenario: After 1 hour, access token expires
 *
 * CLIENT DETECTS EXPIRATION:                  SERVER
 *
 * if (now > tokenExpiration) {
 *   // Token expired
 *   POST /auth/refresh
 *   {
 *     "refreshToken": "..."
 *   }
 *   ────────────────────────────────────────→  AuthController.refreshToken()
 *                                              │
 *                                              └→ AuthServiceImpl.refreshToken()
 *                                                  │
 *                                                  ├→ JwtUtil.validateToken(refreshToken)
 *                                                  │    └─ Check signature & expiration
 *                                                  │       └─ Return: true (not expired)
 *                                                  │
 *                                                  ├→ JwtUtil.isRefreshToken(refreshToken)
 *                                                  │    ├→ JwtUtil.extractTokenType()
 *                                                  │    └─ Verify type == "refresh"
 *                                                  │       └─ Return: true
 *                                                  │
 *                                                  ├→ JwtUtil.extractUsername(refreshToken)
 *                                                  │    └─ Extract sub: "admin"
 *                                                  │
 *                                                  ├→ JwtUtil.extractTokenId(refreshToken)
 *                                                  │    └─ Extract jti: "unique-uuid-2"
 *                                                  │
 *                                                  ├→ RefreshTokenService.isRefreshTokenValid()
 *                                                  │    │
 *                                                  │    ├→ Check if "unique-uuid-2" in store
 *                                                  │    │    └─ YES, found
 *                                                  │    │
 *                                                  │    ├→ Check if expirationTime > now
 *                                                  │    │    └─ YES, not expired
 *                                                  │    │
 *                                                  │    ├→ Check if username matches
 *                                                  │    │    └─ YES, "admin" == "admin"
 *                                                  │    │
 *                                                  │    └─ Return: true (valid)
 *                                                  │
 *                                                  ├→ userRepository.findByUsername("admin")
 *                                                  │    └─ Get user from database
 *                                                  │
 *                                                  ├→ JwtUtil.generateToken("admin")
 *                                                  │    └─ Create NEW access token with:
 *                                                  │       • sub: "admin"
 *                                                  │       • jti: "BRAND-NEW-UUID-3" ← Different!
 *                                                  │       • type: "access"
 *                                                  │       • exp: now + 1 hour
 *                                                  │
 * ←────────────────────────────────────────  200 OK
 * UPDATE TOKEN:                             {
 *   accessToken = "...NEW..."                 token: "...new token...",
 *   refreshToken = "...SAME..."               expiresIn: 3600000,
 *   tokenExpiration = now + 1 hour            refreshToken: "...same...",
 *   refreshExpiration = now + 7 days          ...
 *                                             }
 *
 * // Continue using application
 * GET /api/doctors
 * Authorization: Bearer {NEW_ACCESS_TOKEN}
 * }
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 4. LOGOUT FLOW (Blacklist Token & Revoke Sessions)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT                                      SERVER
 *
 * POST /auth/logout
 * Authorization: Bearer {accessToken}
 * ────────────────────────────────────────→  AuthController.logout()
 *                                              │
 *                                              ├→ Extract token from Authorization header
 *                                              │    └─ token = "..."
 *                                              │
 *                                              ├→ Get username from SecurityContext
 *                                              │    └─ username = "admin"
 *                                              │
 *                                              └→ AuthServiceImpl.logout()
 *                                                  │
 *                                                  ├→ JwtUtil.extractTokenId(token)
 *                                                  │    └─ Extract jti: "unique-uuid-1"
 *                                                  │
 *                                                  ├→ JwtUtil.extractExpiration(token)
 *                                                  │    └─ Extract exp: 1647867600
 *                                                  │
 *                                                  ├→ TokenBlacklistService.blacklistToken()
 *                                                  │    └─ Store in blacklist:
 *                                                  │       {
 *                                                  │         "unique-uuid-1": 1647867600
 *                                                  │       }
 *                                                  │       ← Token marked as revoked
 *                                                  │
 *                                                  ├→ RefreshTokenService.revokeAllRefreshTokensForUser()
 *                                                  │    └─ Remove all tokens for "admin":
 *                                                  │       {
 *                                                  │         "unique-uuid-2": {...} ← DELETED
 *                                                  │       }
 *                                                  │       ← All refresh tokens revoked
 *                                                  │
 *                                                  ├→ SecurityContextHolder.clearContext()
 *                                                  │    └─ Clear authentication on server
 *                                                  │
 * ←────────────────────────────────────────  200 OK
 * DELETE TOKENS:                            {
 *   localStorage.removeItem('accessToken')     message: "Logout successful.
 *   localStorage.removeItem('refreshToken')                Session ended."
 *   localStorage.removeItem('tokenExp')      }
 * }
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 5. AFTER LOGOUT - TOKEN REJECTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT TRIES TO USE OLD TOKEN               SERVER
 *
 * GET /api/doctors
 * Authorization: Bearer {OLD_ACCESS_TOKEN}
 * ────────────────────────────────────────→  Spring DispatcherServlet
 *                                              │
 *                                              ├→ JwtFilter.doFilterInternal()
 *                                              │    │
 *                                              │    ├→ Extract token
 *                                              │    │
 *                                              │    ├→ Extract jti: "unique-uuid-1"
 *                                              │    │
 *                                              │    ├→ TokenBlacklistService.isTokenBlacklisted()
 *                                              │    │    └─ Check blacklist:
 *                                              │    │       {
 *                                              │    │         "unique-uuid-1": 1647867600  ← FOUND!
 *                                              │    │       }
 *                                              │    │
 *                                              │    ├→ Return: true (IS blacklisted)
 *                                              │    │
 *                                              │    ├→ LOG: Token blacklisted for user
 *                                              │    │
 *                                              │    └→ Skip to filterChain.doFilter()
 *                                              │         → Request continues WITHOUT auth
 *                                              │
 *                                              ├→ Spring Security checks authorization
 *                                              │    └─ No authentication set
 *                                              │       └─ Request requires authentication
 *                                              │
 * ←────────────────────────────────────────  403 Forbidden
 * CANNOT ACCESS RESOURCE               (Access Denied)
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 6. AFTER LOGOUT - REFRESH TOKEN ALSO REVOKED
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT TRIES TO REFRESH                    SERVER
 *
 * POST /auth/refresh
 * {
 *   "refreshToken": "{OLD_REFRESH_TOKEN}"
 * }
 * ────────────────────────────────────────→  AuthController.refreshToken()
 *                                              │
 *                                              └→ AuthServiceImpl.refreshToken()
 *                                                  │
 *                                                  ├→ JwtUtil.validateToken()
 *                                                  │    └─ Signature & exp OK
 *                                                  │
 *                                                  ├→ JwtUtil.isRefreshToken()
 *                                                  │    └─ YES, is refresh token
 *                                                  │
 *                                                  ├→ Extract username & jti
 *                                                  │    └─ username: "admin", jti: "unique-uuid-2"
 *                                                  │
 *                                                  ├→ RefreshTokenService.isRefreshTokenValid()
 *                                                  │    │
 *                                                  │    ├→ Check if "unique-uuid-2" in store
 *                                                  │    │    └─ NO! Token was deleted on logout
 *                                                  │    │
 *                                                  │    └─ Return: false (NOT valid)
 *                                                  │
 *                                                  ├→ Throw ResourceNotFoundException()
 *                                                  │    └─ "Refresh token is revoked or invalid"
 *                                                  │
 * ←────────────────────────────────────────  404 Not Found
 * CANNOT REFRESH TOKEN                  {
 *                                           message: "Refresh token is revoked or invalid"
 *                                         }
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * 7. RE-LOGIN AFTER LOGOUT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CLIENT LOGS IN AGAIN                        SERVER
 *
 * POST /auth/login
 * {
 *   "usernameOrEmail": "admin",
 *   "password": "admin123"
 * }
 * ────────────────────────────────────────→  AuthController.login()
 *                                              │
 *                                              └→ AuthServiceImpl.login()
 *                                                  ├→ Authenticate credentials ✓
 *                                                  ├→ Generate accessToken with jti: "new-uuid-4" ← Different!
 *                                                  ├→ Generate refreshToken with jti: "new-uuid-5" ← Different!
 *                                                  └→ Store new refreshToken
 *
 * ←────────────────────────────────────────  200 OK
 * SAVE NEW TOKENS:                          New access & refresh tokens
 *   accessToken = "...NEW..."
 *   refreshToken = "...NEW..."
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * AUTOMATIC CLEANUP PROCESSES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * EVERY HOUR:
 *
 * TokenBlacklistService.cleanupExpiredTokens()  (scheduled @Scheduled)
 *   └─ Runs every 3600000 ms (1 hour)
 *      │
 *      ├→ Iterate through blacklist entries
 *      ├→ Find entries where expirationTime < currentTime
 *      └─ Remove expired entries
 *         └─ Prevents memory growth
 *
 * RefreshTokenService.cleanupExpiredTokens()
 *   └─ Remove refresh tokens with expired expirationTime
 *      └─ Keeps memory usage under control
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * SECURITY FEATURES IN ACTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * 1. SIGNATURE VERIFICATION
 *    Each token has HMAC-SHA512 signature
 *    Verified with jwt.secret from properties
 *    If token is tampered: signature invalid → rejection
 *
 * 2. EXPIRATION CHECKING
 *    exp claim checked on validation
 *    After 1 hour: access token expires
 *    After 7 days: refresh token expires
 *
 * 3. BLACKLISTING
 *    On logout: token added to blacklist
 *    Before processing ANY request: check blacklist
 *    Blacklisted tokens CANNOT be used regardless of signature
 *
 * 4. REFRESH TOKEN STORAGE
 *    Refresh tokens stored server-side
 *    Client sends token ID (jti)
 *    Server validates it exists & not revoked
 *
 * 5. PASSWORD HASHING
 *    Passwords never stored plain
 *    BCrypt hashing on creation
 *    BCrypt comparison on login
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * END OF FLOW DIAGRAM
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class JwtFlowDiagram {
    // This is a documentation class showing complete JWT flow
}

