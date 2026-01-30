package kz.sdu.dto;

import lombok.Builder;

@Builder
public record UpdateVerificationCodeRequestDto(String email, String code) {
}
