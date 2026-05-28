package com.expensetracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts every HTTP request and validates JWT.
 *
 * Flow:
 * 1. Extract JWT from "Authorization: Bearer <token>" header
 * 2. Extract username (email) from token
 * 3. Load user details from database
 * 4. Validate token against user details
 * 5. Set authentication in SecurityContext (marks user as authenticated)
 *
 * OncePerRequestFilter = guaranteed to run exactly once per request
 */
@Component
@RequiredArgsConstructor  // Lombok: creates constructor for all final fields (dependency injection)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain  // Chain to pass request to next filter or controller
    ) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or doesn't start with "Bearer ", skip JWT processing
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to next filter
            return;
        }

        // Step 2: Extract the JWT token (remove "Bearer " prefix, starting at index 7)
        final String jwt = authHeader.substring(7);

        try {
            // Step 3: Extract username (email) from token
            final String userEmail = jwtUtil.extractUsername(jwt);

            // Step 4: If we have an email and the user is NOT already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 5: Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Step 6: Validate the token
                if (jwtUtil.isTokenValid(jwt, userDetails)) {

                    // Step 7: Create authentication token and set it in SecurityContext
                    // This tells Spring Security: "This user is authenticated"
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,     // principal (who is this?)
                            null,            // credentials (no password needed, we used JWT)
                            userDetails.getAuthorities() // what can they do? (roles)
                    );

                    // Add request details (IP address, etc.) to the authentication
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set in SecurityContext so Spring knows the user is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If token is invalid/expired, we just don't authenticate
            // The request will be rejected by Spring Security if it requires authentication
            logger.warn("JWT validation failed: " + e.getMessage());
        }

        // Pass request to the next filter or controller
        filterChain.doFilter(request, response);
    }
}
