package kz.sdu.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record VerifyPasswordResetTokenRequestDto(UUID token) {
}
