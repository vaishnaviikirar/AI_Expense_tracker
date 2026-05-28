package com.expensetracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
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
 * JwtUtil - Utility class for JWT token operations.
 *
 * JWT Structure:
 * header.payload.signature
 *
 * Header: {"alg":"HS256","typ":"JWT"}
 * Payload: {"sub":"user@email.com","iat":1234567,"exp":1234999}
 * Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 *
 * @Component = Spring will auto-detect and register this as a bean
 */
@Component
public class JwtUtil {

    /**
     * Secret key from application.properties.
     * @Value injects the value at application startup.
     */
    @Value("${app.jwt.secret}")
    private String secretKey;

    /**
     * Token expiration time in milliseconds (from application.properties).
     * Default: 86400000 ms = 24 hours
     */
    @Value("${app.jwt.expiration}")
    private long expirationTime;

    // ==========================================
    // TOKEN GENERATION
    // ==========================================

    /**
     * Generate a new JWT token for the given user.
     * The token contains the user's email as the subject.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails.getUsername());
    }

    /**
     * Build and sign the JWT token.
     */
    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(subject)                          // email goes here
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign with our secret
                .compact();
    }

    // ==========================================
    // TOKEN VALIDATION
    // ==========================================

    /**
     * Validate a JWT token.
     * Checks: 1) token belongs to this user, 2) token is not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Check if token is expired.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ==========================================
    // TOKEN EXTRACTION
    // ==========================================

    /**
     * Extract the username (email) from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract the expiration date from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token.
     * Uses Java Function to allow flexible claim extraction.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and extract all claims from the token.
     * If the token is invalid or tampered, this throws an exception.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())  // use our secret to verify signature
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ==========================================
    // KEY MANAGEMENT
    // ==========================================

    /**
     * Get the signing key from the secret string.
     * We decode the Base64-encoded secret and create an HMAC-SHA key.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
