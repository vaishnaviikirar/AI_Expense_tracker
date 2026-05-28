package com.expensetracker.config;

import com.expensetracker.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - The main Spring Security configuration.
 *
 * This class defines:
 * 1. Which endpoints are public (no JWT needed)
 * 2. Which endpoints are secured (JWT required)
 * 3. How to encode passwords (BCrypt)
 * 4. Stateless session management
 * 5. CORS configuration (for frontend)
 *
 * @Configuration = This class defines Spring beans
 * @EnableWebSecurity = Activates Spring Security's web security support
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Main security filter chain configuration.
     * This is where we define security rules for all HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (not needed for stateless REST APIs)
            .csrf(csrf -> csrf.disable())

            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define which requests need authentication
//            .authorizeHttpRequests(auth -> auth
//                // PUBLIC endpoints - no JWT required
//                .requestMatchers("/api/auth/**").permitAll()
//
//                // All other requests need a valid JWT token
//                .anyRequest().authenticated()
//            )

                .authorizeHttpRequests(auth -> auth

                        // Public API endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public frontend pages
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/dashboard.html",
                                "/auth.js",
                                "/dashboard.js",
                                "/styles.css",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Everything else requires JWT
                        .anyRequest().authenticated()
                )

            // Stateless sessions - no HTTP sessions, every request must have JWT
            // This is the key to JWT-based authentication
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Set our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE the standard username/password filter
            // This means: check JWT first, then proceed
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Provider.
     * Tells Spring Security HOW to authenticate users:
     * 1. Use our UserDetailsService to load users
     * 2. Use BCrypt to verify passwords
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Password Encoder Bean.
     * BCrypt is a strong hashing algorithm for passwords.
     * It automatically salts passwords (prevents rainbow table attacks).
     * Cost factor defaults to 10, making brute force attacks slow.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean.
     * Used in AuthController to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS Configuration.
     * Allows the frontend (running on different port) to call our API.
     *
     * Without CORS: browser blocks cross-origin requests.
     * With CORS: we explicitly allow it.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from any origin (in production, specify your frontend URL)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow these headers
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all paths
        return source;
    }
}
