package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse — response body for POST /api/auth/login and POST /api/auth/register
 *
 * Returns the JWT token and basic user info so the client doesn't need
 * a separate "get current user" call after logging in.
 *
 * Sample response:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "userId": 1,
 *   "name": "Yash Sangale",
 *   "email": "yash@example.com",
 *   "role": "CUSTOMER"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
}