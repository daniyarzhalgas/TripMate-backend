package kz.sdu.controller;

import jakarta.mail.MessagingException;
import kz.sdu.dto.*;
import kz.sdu.entity.EmailVerificationCode;
import kz.sdu.entity.PasswordResetToken;
import kz.sdu.service.EmailService;
import kz.sdu.service.VerificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notification")
@AllArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final VerificationService verificationService;

    @PostMapping("/verify-email")
    public void sendVerificationCode(@RequestBody EmailVerificationRequestDto emailVerificationRequestDto) throws MessagingException {
        emailService.sendVerificationCode(emailVerificationRequestDto.email(), emailVerificationRequestDto.code());
    }

    @PostMapping("/verification-code/create")
    public void createVerificationCode(@RequestBody CreateVerificationCodeRequestDto request) throws MessagingException {
        verificationService.createVerificationCode(request.email(), request.code(), request.rawPassword());
        emailService.sendVerificationCode(request.email(), request.code());
    }

    @PostMapping("/verification-code/verify")
    public ResponseEntity<VerifyCodeResponseDto> verifyCode(@RequestBody VerifyCodeRequestDto request) throws MessagingException {
        EmailVerificationCode code = verificationService.getVerificationCode(request.email())
                .orElse(null);

        if (code == null || code.getExpiresAt().isBefore(LocalDateTime.now()) || !code.getCode().equals(request.code())) {
            return ResponseEntity.ok(VerifyCodeResponseDto.builder()
                    .valid(false)
                    .rawPassword(null)
                    .build());
        }


        return ResponseEntity.ok(VerifyCodeResponseDto.builder()
                .valid(true)
                .rawPassword(code.getRawPassword())
                .build());
    }

    @PostMapping("/verification-code/update")
    public void updateVerificationCode(@RequestBody UpdateVerificationCodeRequestDto request) throws MessagingException {
        verificationService.updateVerificationCode(request.email(), request.code());
        emailService.sendVerificationCode(request.email(), request.code());
    }

    @GetMapping("/verification-code/{email}/exists")
    public ResponseEntity<Boolean> verificationCodeExists(@PathVariable("email") String email) {
        return ResponseEntity.ok(verificationService.verificationCodeExists(email));
    }

    @DeleteMapping("/verification-code/{email}")
    public void deleteVerificationCode(@PathVariable("email") String email) {
        verificationService.deleteVerificationCode(email);
    }

    @PostMapping("/password-reset-token/create")
    public ResponseEntity<CreatePasswordResetTokenResponseDto> createPasswordResetToken(@RequestBody CreatePasswordResetTokenRequestDto request) {
        UUID token = verificationService.createPasswordResetToken(request.email());
        emailService.sendPasswordResetLink(request.email(), token.toString());
        return ResponseEntity.ok(CreatePasswordResetTokenResponseDto.builder()
                .token(token)
                .build());
    }

    @PostMapping("/password-reset-token/verify")
    public ResponseEntity<VerifyPasswordResetTokenResponseDto> verifyPasswordResetToken(@RequestBody VerifyPasswordResetTokenRequestDto request) {
        PasswordResetToken token = verificationService.getPasswordResetToken(request.token())
                .orElse(null);

        if (token == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(VerifyPasswordResetTokenResponseDto.builder()
                    .valid(false)
                    .email(null)
                    .build());
        }

        return ResponseEntity.ok(VerifyPasswordResetTokenResponseDto.builder()
                .valid(true)
                .email(token.getEmail())
                .build());
    }

    @DeleteMapping("/password-reset-token/{token}")
    public void deletePasswordResetToken(@PathVariable("token") UUID token) {
        verificationService.deletePasswordResetToken(token);
    }
}
