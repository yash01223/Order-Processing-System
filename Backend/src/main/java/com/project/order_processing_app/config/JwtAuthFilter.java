package com.project.order_processing_app.config;

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
 * JwtAuthFilter — intercepts every HTTP request and validates the JWT token.
 *
 * Extends OncePerRequestFilter to guarantee this filter runs exactly ONCE
 * per request (Spring may forward/dispatch requests internally causing filters
 * to run multiple times without this base class).
 *
 * ─────────────────────────────────────────────────────────────
 * REQUEST FLOW:
 * ─────────────────────────────────────────────────────────────
 *  HTTP Request
 *    → JwtAuthFilter.doFilterInternal()
 *        1. Extract "Authorization" header
 *        2. If missing or not "Bearer ...", skip (let SecurityConfig handle the 401)
 *        3. Extract email from token via JwtUtil
 *        4. Load User from DB by email
 *        5. Validate token (signature + expiry + email match)
 *        6. Set UsernamePasswordAuthenticationToken into SecurityContext
 *    → Controller method runs with authenticated user in context
 *        → @AuthenticationPrincipal User user works in controller params
 *        → @PreAuthorize("hasRole('ADMIN')") works for role guards
 * ─────────────────────────────────────────────────────────────
 */
@Component
@RequiredArgsConstructor  // Lombok: generates constructor for all final fields (constructor injection)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    /**
     * Core filter logic — runs on every HTTP request.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response (not modified here)
     * @param filterChain the remaining filters to execute after this one
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ── Step 1: Extract the Authorization header ──────────────────
        final String authHeader = request.getHeader("Authorization");

        // ── Step 2: Skip filter if no Bearer token present
        // Public endpoints (/api/auth/**) have no token → pass through.
        // SecurityConfig's permitAll() rules will allow them without auth.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 3: Extract the raw JWT string
        // Header format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        // We strip the "Bearer " prefix (7 characters) to get the token.
        final String jwt = authHeader.substring(7);

        // ── Step 4: Extract email from the token's "sub" claim
        final String userEmail;
        try {
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Malformed or tampered token — skip auth, let SecurityConfig return 401
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 5: Authenticate if email found and not already authenticated ──
        // SecurityContextHolder.getContext().getAuthentication() is non-null if the
        // user was already authenticated earlier in this request (shouldn't happen
        // with OncePerRequestFilter, but this is the standard defensive check).
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load full UserDetails from DB (triggers SELECT * FROM users WHERE email = ?)
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // ── Step 6: Validate token against loaded user
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // Create an authentication token with:
                //   principal    = userDetails (our User entity)
                //   credentials  = null (we don't need the password after authentication)
                //   authorities  = user's granted roles (e.g., [ROLE_CUSTOMER])
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request metadata (IP address, session ID) to the auth token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ── Step 7: Set authentication in the SecurityContext ──
                // From this point forward, any code that calls
                // SecurityContextHolder.getContext().getAuthentication()
                // will get this authenticated user.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── Step 8: Continue to the next filter / controller ──────────
        filterChain.doFilter(request, response);
    }
}