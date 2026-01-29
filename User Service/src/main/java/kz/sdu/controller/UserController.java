package kz.sdu.controller;

import jakarta.validation.Valid;
import kz.sdu.dto.*;
import kz.sdu.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto req) {
        return userService.login(req);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(@Valid @RequestBody UserRegistrationDto req) {
        return userService.register(req);
    }

    @PostMapping("/verify-email")
    public VerifyEmailResponseDto verifyEmail(@Valid @RequestBody VerifyEmailRequestDto req) {
        return userService.verifyEmail(req);
    }

    @PostMapping("/resend-verification")
    public SimpleMessageResponseDto resendVerification(@Valid @RequestBody ResendVerificationRequestDto req) {
        return userService.resendVerification(req);
    }

    @PostMapping("/forgot-password")
    public SimpleMessageResponseDto forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto req) {
        return userService.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public SimpleMessageResponseDto resetPassword(@Valid @RequestBody ResetPasswordRequestDto req) {
        return userService.resetPassword(req);
    }

    @PostMapping("/refresh-token")
    public RefreshTokenResponseDto refreshToken(@Valid @RequestBody RefreshTokenRequestDto req) {
        return userService.refreshToken(req);
    }

    @PostMapping("/logout")
    public SimpleMessageResponseDto logout(@Valid @RequestBody LogoutRequestDto req) {
        return userService.logout(req);
    }

    @PostMapping("/google")
    public UserLoginResponseDto google(@Valid @RequestBody GoogleAuthRequestDto req) {
        return userService.google(req);
    }
}
