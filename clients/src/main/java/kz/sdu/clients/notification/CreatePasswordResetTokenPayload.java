package kz.sdu.clients.notification;

import lombok.Builder;

@Builder
public record CreatePasswordResetTokenPayload(String email) {
}
