package kz.sdu.dto;

import lombok.Builder;

@Builder
public record VerifyCodeRequestDto(String email, String code) {
}
