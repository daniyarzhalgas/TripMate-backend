package kz.sdu.dto;

import lombok.Builder;

@Builder
public record CreateVerificationCodeRequestDto(String email, String code, String rawPassword) {
}
