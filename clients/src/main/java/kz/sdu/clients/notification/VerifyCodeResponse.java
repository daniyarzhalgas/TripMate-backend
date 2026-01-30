package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record VerifyCodeResponse(boolean valid, String rawPassword) {
}
