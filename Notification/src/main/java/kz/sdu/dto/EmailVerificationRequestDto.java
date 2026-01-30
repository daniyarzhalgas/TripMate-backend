package kz.sdu.dto;

import lombok.Builder;

@Builder
public record EmailVerificationRequestDto(String email, String code) {
}
