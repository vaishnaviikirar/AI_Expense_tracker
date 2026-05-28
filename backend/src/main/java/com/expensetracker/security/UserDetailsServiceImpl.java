package com.expensetracker.security;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsServiceImpl - Tells Spring Security HOW to load a user.
 *
 * Spring Security needs a UserDetailsService to:
 * 1. Find the user by username (we use email as username)
 * 2. Return a UserDetails object with password and roles
 * 3. Compare the stored password with the provided one during login
 *
 * This is the bridge between our User entity and Spring Security.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case).
     * Called automatically by Spring Security during authentication.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert our User entity to Spring Security's UserDetails
        // org.springframework.security.core.userdetails.User is a built-in implementation
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())   // BCrypt encrypted password
                .roles("USER")                  // Assign role (we use a single role here)
                .build();
    }
}
