package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record VerifyPasswordResetTokenResponse(boolean valid, String email) {
}
