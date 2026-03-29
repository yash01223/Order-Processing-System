package com.project.order_processing_app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginRequest — request body for POST /api/auth/login
 *
 * On success, the server returns a signed JWT token.
 * The client must store this token and include it in all subsequent requests:
 *   Authorization: Bearer <token>
 *
 * Sample request body:
 * {
 *   "email": "yash@example.com",
 *   "password": "secure123"
 * }
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}