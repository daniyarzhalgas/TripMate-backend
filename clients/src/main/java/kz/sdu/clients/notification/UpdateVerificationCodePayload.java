package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record UpdateVerificationCodePayload(String email, String code) {
}
