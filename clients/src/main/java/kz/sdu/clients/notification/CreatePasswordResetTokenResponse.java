package kz.sdu.clients.notification;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreatePasswordResetTokenResponse(UUID token) {
}
