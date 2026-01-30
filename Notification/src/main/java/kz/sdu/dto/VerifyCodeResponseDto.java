package kz.sdu.dto;

import lombok.Builder;

@Builder
public record VerifyCodeResponseDto(boolean valid, String rawPassword) {
}
