package com.project.order_processing_app.service;

import com.project.order_processing_app.entity.Otp;
import com.project.order_processing_app.exception.InvalidOtpException;
import com.project.order_processing_app.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a 4-digit OTP, hashes it, and saves it to the database.
     * Returns the raw OTP for email delivery.
     */
    @Transactional
    public String generateAndSaveOtp(String email) {
        // Generate 6-digit OTP (e.g. 123456)
        String rawOtp = String.format("%06d", secureRandom.nextInt(1000000));

        // Hash the OTP before storing it
        String otpHash = passwordEncoder.encode(rawOtp);

        Otp otp = Otp.builder()
                .email(email)
                .otpHash(otpHash)
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // 5 minutes expiry
                .isUsed(false)
                .attempts(0)
                .build();

        otpRepository.save(otp);
        log.info("Generated and saved new OTP for email: {}", email);

        return rawOtp;
    }

    /**
     * Validates the provided OTP against the latest record for the email.
     * 
     * @throws RuntimeException if OTP is invalid, expired, or max attempts reached.
     */
    @Transactional
    public void validateOtp(String email, String rawOtp) {
        Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new InvalidOtpException("No OTP found for this email."));

        if (otp.isUsed()) {
            throw new InvalidOtpException("This OTP has already been used.");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new InvalidOtpException("This OTP has expired.");
        }

        if (otp.getAttempts() >= 3) {
            throw new InvalidOtpException("Maximum OTP verification attempts reached. Please request a new one.");
        }

        // Verify hash
        if (!passwordEncoder.matches(rawOtp, otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            log.warn("Invalid OTP attempt for email: {}. Attempts: {}", email, otp.getAttempts());
            throw new InvalidOtpException("Invalid OTP. Attempts left: " + (3 - otp.getAttempts()));
        }

        // Mark as used on success
        otp.setUsed(true);
        otpRepository.save(otp);
        log.info("OTP successfully verified for email: {}", email);
    }

    /**
     * Cleanup task runs every 5 minutes to remove expired OTPs from the database.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Starting cleanup of expired OTPs...");
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleanup completed.");
    }
}
