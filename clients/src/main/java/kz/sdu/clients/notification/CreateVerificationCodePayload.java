package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record CreateVerificationCodePayload(String email, String code, String rawPassword) {
}
