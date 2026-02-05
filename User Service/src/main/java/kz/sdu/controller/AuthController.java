package kz.sdu.controller;

import jakarta.validation.Valid;
import kz.sdu.dto.*;
import kz.sdu.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto req) {
        return authService.login(req);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody UserRegistrationDto req) {
        return authService.register(req);
    }

    @PostMapping("/verify-email")
    public VerifyEmailResponseDto verifyEmail(@Valid @RequestBody VerifyEmailRequestDto req) {
        return authService.verifyEmail(req);
    }

    @PostMapping("/resend-verification")
    public SimpleMessageResponseDto resendVerification(@Valid @RequestBody ResendVerificationRequestDto req) {
        return authService.resendVerification(req);
    }

    @PostMapping("/forgot-password")
    public SimpleMessageResponseDto forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto req) {
        return authService.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public SimpleMessageResponseDto resetPassword(@Valid @RequestBody ResetPasswordRequestDto req) {
        return authService.resetPassword(req);
    }

    @PostMapping("/refresh-token")
    public RefreshTokenResponseDto refreshToken(@Valid @RequestBody RefreshTokenRequestDto req) {
        return authService.refreshToken(req);
    }

    @PostMapping("/logout")
    public SimpleMessageResponseDto logout(@Valid @RequestBody LogoutRequestDto req) {
        return authService.logout(req);
    }

    @PostMapping("/google")
    public UserLoginResponseDto google(@Valid @RequestBody GoogleAuthRequestDto req) {
        return authService.google(req);
    }
}
