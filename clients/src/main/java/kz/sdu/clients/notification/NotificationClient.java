package kz.sdu.clients.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient("notification")
public interface NotificationClient {

    @PostMapping("/api/notification/verify-email")
    void sendVerificationCode(@RequestBody VerificationCodePayload verificationCodePayload);

    @PostMapping("/api/notification/verification-code/create")
    void createVerificationCode(@RequestBody CreateVerificationCodePayload payload);

    @PostMapping("/api/notification/verification-code/verify")
    VerifyCodeResponse verifyCode(@RequestBody VerifyCodePayload payload);

    @PostMapping("/api/notification/verification-code/update")
    void updateVerificationCode(@RequestBody UpdateVerificationCodePayload payload);

    @GetMapping("/api/notification/verification-code/{email}/exists")
    boolean verificationCodeExists(@PathVariable("email") String email);

    @DeleteMapping("/api/notification/verification-code/{email}")
    void deleteVerificationCode(@PathVariable("email") String email);

    @PostMapping("/api/notification/password-reset-token/create")
    CreatePasswordResetTokenResponse createPasswordResetToken(@RequestBody CreatePasswordResetTokenPayload payload);

    @PostMapping("/api/notification/password-reset-token/verify")
    VerifyPasswordResetTokenResponse verifyPasswordResetToken(@RequestBody VerifyPasswordResetTokenPayload payload);

    @DeleteMapping("/api/notification/password-reset-token/{token}")
    void deletePasswordResetToken(@PathVariable("token") UUID token);
}
