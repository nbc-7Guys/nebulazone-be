package nbc.chillguys.nebulazone.infra.oauth.dto;

import lombok.Builder;

@Builder
public record TokensDto(
	String accessToken,
	String refreshToken
) {
}
