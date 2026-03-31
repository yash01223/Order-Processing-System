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
    private final OtpService otpService;
    private final EmailService emailService;

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

        // ── Step 1: Check if user already exists
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user != null) {
            if (user.isVerified()) {
                throw new DuplicateEmailException(request.getEmail());
            }
            // User exists but is not verified — allow them to continue to verification
            // We can optionally update their name/password here if changed
        } else {
            // ── Step 2: Create new unverified User ────────────────────────
            Role userRole = Role.CUSTOMER;
            if (request.getRole() != null && request.getRole().equalsIgnoreCase("Admin")) {
                userRole = Role.ADMIN;
            }

            user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(userRole)
                    .isVerified(false) // Explicitly unverified
                    .build();

            user = userRepository.save(user);
        }

        // ── Step 3: Generate and Send OTP ─────────────────────────────
        String rawOtp = otpService.generateAndSaveOtp(user.getEmail());
        emailService.sendEmailOtp(user.getEmail(), rawOtp);

        // Return response WITHOUT token — user must verify first
        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Verifies the OTP and activates the user account.
     * Returns a JWT token on success.
     */
    @Transactional
    public AuthResponse verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified.");
        }

        // Validate OTP (throws if invalid/expired)
        otpService.validateOtp(email, otp);

        // Mark user as verified
        user.setVerified(true);
        userRepository.save(user);

        // Generate JWT for the now-verified user
        String token = jwtUtil.generateToken(user, user.getId(), user.getRole().name());

        return buildAuthResponse(token, user);
    }

    /**
     * Resends a new OTP to the user's email.
     */
    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified.");
        }

        String rawOtp = otpService.generateAndSaveOtp(email);
        emailService.sendEmailOtp(email, rawOtp);
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