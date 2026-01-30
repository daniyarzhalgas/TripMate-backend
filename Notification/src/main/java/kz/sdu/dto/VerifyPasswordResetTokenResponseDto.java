package kz.sdu.dto;

import lombok.Builder;

@Builder
public record VerifyPasswordResetTokenResponseDto(boolean valid, String email) {
}
