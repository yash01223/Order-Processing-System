package com.project.order_processing_app.config;

import com.project.order_processing_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ApplicationConfig — defines core authentication beans separately from SecurityConfig.
 *
 * ─────────────────────────────────────────────────────────────
 * WHY THIS SPLIT IS NECESSARY (Circular Dependency Problem):
 * ─────────────────────────────────────────────────────────────
 * If UserDetailsService @Bean is defined INSIDE SecurityConfig,
 * Spring creates a circular dependency at startup:
 *
 *   SecurityConfig
 *     └── needs JwtAuthFilter (constructor injection)
 *           └── needs UserDetailsService
 *                 └── defined in SecurityConfig  ← already being created!
 *
 * Spring cannot resolve this — it throws:
 *   "Error creating bean 'jwtAuthFilter': Requested bean is currently in creation"
 *
 * SOLUTION: Move UserDetailsService, PasswordEncoder, AuthenticationProvider,
 * and AuthenticationManager into this separate config class.
 *
 * Now the dependency graph is clean:
 *   ApplicationConfig  →  UserRepository          (no cycle)
 *   JwtAuthFilter      →  UserDetailsService      (from ApplicationConfig)
 *   SecurityConfig     →  JwtAuthFilter            (from Spring context)
 *   SecurityConfig     →  AuthenticationProvider  (from ApplicationConfig)
 * ─────────────────────────────────────────────────────────────
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * UserDetailsService — tells Spring Security how to load a user by email.
     *
     * Defined here (not in SecurityConfig) to break the circular dependency.
     * Spring injects this bean into both JwtAuthFilter and authenticationProvider().
     *
     * Our User entity implements UserDetails directly, so we return
     * it straight from the repository — no wrapper class needed.
     *
     * Generated SQL: SELECT * FROM users WHERE email = ? LIMIT 1
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + username
                ));
    }

    /**
     * PasswordEncoder — BCrypt hashing for passwords.
     *
     * Default cost factor 10 → ~100ms per hash.
     * Used in:
     *   - AuthService.register()  → encoder.encode(rawPassword)
     *   - DaoAuthenticationProvider → encoder.matches(raw, hashed) during login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider — wires UserDetailsService + PasswordEncoder together.
     *
     * DaoAuthenticationProvider is Spring's standard DB-backed provider:
     *   1. Calls userDetailsService.loadUserByUsername(email) to fetch the user
     *   2. Calls passwordEncoder.matches(rawPassword, hashedPassword) to verify
     *   3. Throws BadCredentialsException if either step fails
     *
     * Injected into SecurityConfig to register it in the filter chain.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — the entry point for programmatic authentication.
     *
     * AuthService.login() calls:
     *   authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
     *
     * This internally delegates to DaoAuthenticationProvider defined above.
     * Spring derives the AuthenticationManager from AuthenticationConfiguration automatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
