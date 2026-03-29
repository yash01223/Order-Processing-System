package com.project.order_processing_app.config;

import com.project.order_processing_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — configures the entire Spring Security setup.
 *
 * @Configuration  → Spring reads this class for bean definitions at startup.
 * @EnableWebSecurity → Activates Spring Security's web support.
 * @EnableMethodSecurity → Enables @PreAuthorize on controller methods.
 *                         Without this, @PreAuthorize("hasRole('ADMIN')") does nothing.
 *
 * Key concepts configured here:
 *   1. Which endpoints are public vs protected
 *   2. Stateless session management (JWT means no server-side sessions)
 *   3. How to load users from the DB (UserDetailsService)
 *   4. How to verify passwords (BCrypt)
 *   5. Where to insert the JwtAuthFilter in the filter chain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
//    private final CustomUserDetailsService customUserDetailsService;

    // ═══════════════════════════════════════════════════════════
    //  PasswordEncoder — BCrypt hashing
    // ═══════════════════════════════════════════════════════════

    /**
     * BCryptPasswordEncoder hashes passwords before storing them.
     * Default cost factor: 10 (~100ms per hash — safe against brute force).
     *
     * Used in:
     *   - AuthService.register() → encoder.encode(rawPassword)
     *   - DaoAuthenticationProvider.authenticate() → encoder.matches(raw, hashed)
     */

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return username -> userRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException(
//                        "No user found with email: " + username
//                ));
//    }


//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    // ═══════════════════════════════════════════════════════════
    //  AuthenticationProvider — wires UserDetailsService + PasswordEncoder
    // ═══════════════════════════════════════════════════════════

    /**
     * DaoAuthenticationProvider ties together:
     *   - How to load the user (UserDetailsService)
     *   - How to verify the password (PasswordEncoder)
     *
     * Spring Security calls this during AuthenticationManager.authenticate().
     */
//    @Bean
//    public AuthenticationProvider authenticationProvider(
//            CustomUserDetailsService customUserDetailsService,
//            PasswordEncoder passwordEncoder) {
//
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(customUserDetailsService);
//        provider.setPasswordEncoder(passwordEncoder);
//
//        return provider;
//    }


//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        // Wire the inline lambda bean defined above
//        provider.setUserDetailsService(userDetailsService());
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }

    // ═══════════════════════════════════════════════════════════
    //  AuthenticationManager — used in AuthService to trigger login
    // ═══════════════════════════════════════════════════════════

    /**
     * Exposes the AuthenticationManager as a bean.
     * AuthService.login() calls authenticationManager.authenticate(...)
     * which internally uses DaoAuthenticationProvider defined above.
     */
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }

    // ═══════════════════════════════════════════════════════════
    //  SecurityFilterChain — the main security configuration
    // ═══════════════════════════════════════════════════════════

    /**
     * Configures the HTTP security filter chain.
     *
     * Key decisions:
     *
     *   csrf().disable():
     *     CSRF protection is for browser-based session cookies.
     *     Our API uses stateless JWT tokens — CSRF is not applicable.
     *     Disabling it prevents CSRF token errors on POST/PUT/DELETE requests.
     *
     *   SessionCreationPolicy.STATELESS:
     *     No HttpSession is created or used. The server never stores session state.
     *     Every request is fully authenticated by its JWT token alone.
     *
     *   addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class):
     *     Inserts our JWT filter BEFORE Spring's default login filter.
     *     This ensures the JWT is checked before any default auth logic runs.
     *
     * Endpoint access rules (order matters — first match wins):
     *   POST /api/auth/**     → Public (register, login — no token needed)
     *   GET  /api/products/** → Public (anyone can browse products)
     *   Everything else       → Must be authenticated (valid JWT required)
     *   Role-specific rules   → Enforced by @PreAuthorize on controller methods
     */


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                // ── Disable CSRF (stateless JWT API — no cookies) ─────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Endpoint authorization rules ───────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Public: auth endpoints — no token required
                        .requestMatchers("/auth/**").permitAll()

                        // Public: GET product list and detail — anyone can browse
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                        // Everything else requires a valid JWT token
                        .anyRequest().authenticated()
                )

                // ── No sessions — every request is self-contained ──────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Wire in our custom AuthenticationProvider ───────────────
                .authenticationProvider(authenticationProvider)

                // ── Insert JWT filter before the default login filter ───────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Allows all origins, including null (local file opening)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Required for certain authentication flows
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}