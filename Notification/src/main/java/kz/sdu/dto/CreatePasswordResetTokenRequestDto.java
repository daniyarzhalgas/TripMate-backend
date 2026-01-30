package kz.sdu.dto;

import lombok.Builder;

@Builder
public record CreatePasswordResetTokenRequestDto(String email) {
}
