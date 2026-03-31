package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * InvalidOtpException — thrown when OTP verification fails
 * Due to: incorrect code, expiry, or max attempts reached.
 * 
 * Annotated with @ResponseStatus(HttpStatus.BAD_REQUEST)
 * ensures that even if not manually caught by GlobalExceptionHandler,
 * it returns 400 by default.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
