package kz.sdu.service;

import jakarta.mail.MessagingException;
import kz.sdu.entity.EmailVerificationCode;
import kz.sdu.entity.PasswordResetToken;
import kz.sdu.repository.EmailVerificationCodeRepository;
import kz.sdu.repository.PasswordResetTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VerificationService {

    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoggingEmailService loggingEmailService;

    @Transactional
    public void createVerificationCode(String email, String code, String rawPassword) {
        emailVerificationCodeRepository.save(EmailVerificationCode.builder()
                .email(email)
                .code(code)
                .rawPassword(rawPassword)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());
    }

    public Optional<EmailVerificationCode> getVerificationCode(String email) throws MessagingException {
        EmailVerificationCode code = emailVerificationCodeRepository.findById(email).orElseThrow(() -> new NoSuchElementException("email not exits"));

        loggingEmailService.sendWelcomeMessage(email);
        return Optional.of(code);
    }

    public boolean verificationCodeExists(String email) {
        return emailVerificationCodeRepository.existsById(email);
    }

    @Transactional
    public void deleteVerificationCode(String email) {
        emailVerificationCodeRepository.deleteById(email);
    }

    @Transactional
    public void updateVerificationCode(String email, String newCode) {
        EmailVerificationCode existing = emailVerificationCodeRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Verification code not found"));
        existing.setCode(newCode);
        existing.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        emailVerificationCodeRepository.save(existing);
    }

    @Transactional
    public UUID createPasswordResetToken(String email) {
        UUID token = UUID.randomUUID();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .email(email)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build());
        return token;
    }

    public Optional<PasswordResetToken> getPasswordResetToken(UUID token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
    public void deletePasswordResetToken(UUID token) {
        passwordResetTokenRepository.deleteById(token);
    }
}
