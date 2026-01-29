package kz.sdu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendVerificationCode(String email, String code) {
        log.info("Verification code for {}: {}", email, code);
    }

    @Override
    public void sendPasswordResetLink(String email, String token) {
        log.info("Password reset token for {}: {}", email, token);
    }
}

