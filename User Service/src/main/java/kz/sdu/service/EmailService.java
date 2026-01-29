package kz.sdu.service;

public interface EmailService {
    void sendVerificationCode(String email, String code);

    void sendPasswordResetLink(String email, String token);
}

