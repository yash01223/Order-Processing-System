package com.project.order_processing_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmailOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Email Verification — Your OTP");
            
            String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>" +
                "  <h2>Email Verification</h2>" +
                "  <p>Hello,</p>" +
                "  <p>Please use the following 6-digit OTP to verify your email address. It will expire in 5 minutes.</p>" +
                "  <div style='font-size: 24px; font-weight: bold; padding: 10px; background-color: #f4f4f4; border-radius: 5px; display: inline-block;'>%s</div>" +
                "  <p>If you didn't request this, you can safely ignore this email.</p>" +
                "  <p>Best regards,<br/>Order Processing System</p>" +
                "</div>", otp
            );

            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);
            log.info("Successfully sent OTP email to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Could not send verification email. Please try again later.");
        }
    }
}
