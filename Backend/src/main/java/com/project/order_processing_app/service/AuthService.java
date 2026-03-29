package com.project.order_processing_app.service;

import com.project.order_processing_app.dto.request.LoginRequest;
import com.project.order_processing_app.dto.request.RegisterRequest;
import com.project.order_processing_app.dto.response.AuthResponse;
import com.project.order_processing_app.exception.DuplicateEmailException;
import com.project.order_processing_app.entity.User;
import com.project.order_processing_app.entity.Role;
import com.project.order_processing_app.repository.UserRepository;
import com.project.order_processing_app.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — business logic for registration and login.
 *
 * @Service marks this as a Spring service bean (a specialization
 *          of @Component).
 * @RequiredArgsConstructor generates a constructor for all final fields —
 *                          Spring
 *                          uses constructor injection (recommended
 *                          over @Autowired field injection).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ═══════════════════════════════════════════════════════════
    // REGISTER — POST /api/auth/register
    // ═══════════════════════════════════════════════════════════

    /**
     * Registers a new customer account.
     *
     * Steps:
     * 1. Check the email isn't already registered
     * 2. Hash the password with BCrypt
     * 3. Save the new User entity with CUSTOMER role
     * 4. Generate and return a JWT so the user is logged in immediately
     *
     * @Transactional: if the save fails (e.g., DB constraint violation),
     *                 the entire operation rolls back — no partial state.
     *
     * @param request validated RegisterRequest from the controller
     * @return AuthResponse containing the JWT token and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // ── Step 1: Guard against duplicate emails
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // ── Step 2: Build and persist the User entity ──────────────────
        Role userRole = Role.CUSTOMER; // Default
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("Admin")) {
            userRole = Role.ADMIN;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                // BCrypt encodes the raw password: $2a$10$<salt><hash>
                // The original password is never stored
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        User savedUser = userRepository.save(user);

        // ── Step 3: Generate JWT for immediate login after registration ─
        String token = jwtUtil.generateToken(savedUser, savedUser.getId(), savedUser.getRole().name());

        return buildAuthResponse(token, savedUser);
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIN — POST /api/auth/login
    // ═══════════════════════════════════════════════════════════

    /**
     * Authenticates a user and returns a JWT token.
     *
     * Steps:
     * 1. Delegate to Spring Security's AuthenticationManager
     * 2. AuthenticationManager calls DaoAuthenticationProvider:
     * → Loads user by email via UserDetailsService
     * → Verifies password with BCryptPasswordEncoder.matches()
     * → Throws BadCredentialsException if either fails
     * 3. Extract authenticated User from the Authentication object
     * 4. Generate and return a JWT token
     *
     * We let Spring Security handle credential verification — this is
     * more secure than doing the check manually (handles timing attacks, etc.)
     *
     * @param request validated LoginRequest with email and password
     * @return AuthResponse containing the JWT token
     */
    public AuthResponse login(LoginRequest request) {

        // ── Step 1: Authenticate via Spring Security ───────────────────
        // Throws BadCredentialsException if email not found or password wrong.
        // GlobalExceptionHandler catches this and returns HTTP 401.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // ── Step 2: Extract the authenticated User from the result ──────
        // The principal is our User entity (implements UserDetails)
        User user = (User) authentication.getPrincipal();

        // ── Step 3: Generate JWT and build response ─────────────────────
        String token = jwtUtil.generateToken(user, user.getId(), user.getRole().name());

        return buildAuthResponse(token, user);
    }

    // ═══════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════

    /** Builds the AuthResponse DTO from a token and a User entity */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}