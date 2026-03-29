package com.project.order_processing_app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RegisterRequest — request body for POST /api/auth/register
 *
 * @Data generates: @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
 *
 *       Validation annotations are processed by Spring's @Valid before the
 *       method body runs.
 *       If any constraint fails, MethodArgumentNotValidException is thrown and
 *       caught by
 *       GlobalExceptionHandler, which returns a clean 400 Bad Request with
 *       field-level error messages.
 *
 *       Sample request body:
 *       {
 *       "name": "Yash Sangale",
 *       "email": "yash@example.com",
 *       "password": "secure123"
 *       }
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String role; // optional
}
