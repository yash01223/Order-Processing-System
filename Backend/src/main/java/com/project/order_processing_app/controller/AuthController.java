package com.project.order_processing_app.controller;

import com.project.order_processing_app.dto.request.LoginRequest;
import com.project.order_processing_app.dto.request.RegisterRequest;
import com.project.order_processing_app.dto.response.AuthResponse;
import com.project.order_processing_app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles user registration and login.
 *
 * Base path: /api/auth  (context-path /api + @RequestMapping /auth)
 * Both endpoints are PUBLIC — no JWT token required.
 * Configured in SecurityConfig: .requestMatchers("/auth/**").permitAll()
 *
 * @RestController = @Controller + @ResponseBody
 *   → Every method return value is serialized to JSON automatically.
 *
 * Controllers are kept THIN:
 *   - Extract and validate the request body
 *   - Delegate ALL business logic to the service
 *   - Wrap the result in the appropriate ResponseEntity with HTTP status
 *   - Never contain if/else business logic or direct DB calls
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        // 201 Created — a new resource was created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}