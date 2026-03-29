package com.project.order_processing_app.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil — utility class for creating and validating JWT tokens.
 * 
 * ────────────────────────────────────────────────────────────
 * HOW JWT WORKS IN THIS APP:
 * ────────────────────────────────────────────────────────────
 * 1. User logs in → AuthService calls generateToken(user)
 * 2. Token is returned to the client in the login response
 * 3. Client stores the token and sends it on every subsequent request:
 * Authorization: Bearer <token>
 * 4. JwtAuthFilter intercepts every request, calls validateToken()
 * 5. If valid, the user is set in Spring Security's SecurityContext
 * 6. Controllers then see an authenticated user with their role
 *
 * JWT STRUCTURE (3 Base64-encoded parts separated by dots):
 * Header → {"alg": "HS256", "typ": "JWT"}
 * Payload → {"sub": "user@email.com", "userId": 1, "role": "CUSTOMER", "iat":
 * ..., "exp": ...}
 * Signature → HMAC-SHA256(header + "." + payload, secretKey)
 *
 * The server never stores tokens — validation is done purely by
 * verifying the signature with the secret key (stateless).
 * ─────────────────────────────────────────────────────────────
 *
 * @Component makes this a Spring-managed bean — injectable via @Autowired or
 *            constructor injection.
 */
@Component
public class JwtUtil {

    /**
     * Secret key string injected from application.properties (jwt.secret).
     * Must be at least 256 bits (32 chars) for the HS256 algorithm.
     * In production: inject via environment variable, NEVER hardcode.
     */
    @Value("${jwt.secret}")
    private String secretString;

    /**
     * Token validity in milliseconds — injected from application.properties
     * (jwt.expiration).
     * Default: 86400000 ms = 24 hours.
     */
    @Value("${jwt.expiration}")
    private long expirationMs;

    // ═══════════════════════════════════════════════════════════
    // Token Generation
    // ═══════════════════════════════════════════════════════════

    /**
     * Generates a signed JWT token for the given user.
     *
     * Token claims (payload data):
     * sub → email (standard JWT "subject" claim — used to look up user on each
     * request)
     * userId → user's DB id (avoids an extra DB lookup in some scenarios)
     * role → user's role (CUSTOMER or ADMIN) — used for quick role checks
     * iat → issued-at timestamp
     * exp → expiry timestamp (iat + expirationMs)
     *
     * @param userDetails the authenticated user (our User entity implements
     *                    UserDetails)
     * @param userId      the user's DB primary key
     * @param role        the user's role as a string
     * @return signed JWT string — e.g. "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi..."
     */
    public String generateToken(UserDetails userDetails, Long userId, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId); // Embed userId so we don't always need a DB lookup
        extraClaims.put("role", role); // Embed role for fast authorization decisions

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // subject = email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ═══════════════════════════════════════════════════════════
    // Token Validation
    // ═══════════════════════════════════════════════════════════

    /**
     * Validates a JWT token against a loaded UserDetails object.
     *
     * Checks:
     * 1. Token signature is valid (not tampered with)
     * 2. Token is not expired
     * 3. Token subject (email) matches the loaded user's username
     *
     * @param token       raw JWT string from the Authorization header
     * @param userDetails user loaded from DB by email extracted from the token
     * @return true if the token is valid and belongs to this user
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ═══════════════════════════════════════════════════════════
    // Claim Extraction
    // ═══════════════════════════════════════════════════════════

    /**
     * Extracts the username (email) from the token's "sub" claim.
     * Used by JwtAuthFilter to know which user to load from the DB.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the token.
     * Used internally to check if the token has expired.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor — takes a function to apply to the Claims object.
     * This pattern avoids duplicating the token parsing logic for every claim.
     *
     * Example: extractClaim(token, Claims::getSubject) → returns the "sub" string
     *
     * @param token          raw JWT string
     * @param claimsResolver function that picks a specific claim value
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ═══════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════

    /**
     * Parses and verifies the token signature, returning all claims.
     *
     * Throws JwtException subtypes if anything is wrong:
     * - ExpiredJwtException → token is past its expiry date
     * - MalformedJwtException → token structure is invalid
     * - SignatureException → token was tampered with (signature mismatch)
     * - UnsupportedJwtException → algorithm not supported
     *
     * These are caught in JwtAuthFilter and result in a 401 Unauthorized response.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Returns true if the token's expiration is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Converts the plain-text secret string to a cryptographic Key object.
     *
     * Keys.hmacShaKeyFor() creates an HMAC-SHA key from the byte array.
     * Called on every token operation — the key is derived from secretString each
     * time.
     * (In a high-traffic system, this could be cached as a field, but the overhead
     * is minimal.)
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }
}