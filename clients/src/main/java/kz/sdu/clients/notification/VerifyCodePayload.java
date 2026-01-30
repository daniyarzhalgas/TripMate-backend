package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record VerifyCodePayload(String email, String code) {
}
