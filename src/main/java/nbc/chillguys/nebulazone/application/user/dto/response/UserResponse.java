package nbc.chillguys.nebulazone.application.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import nbc.chillguys.nebulazone.domain.user.dto.UserInfo;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
	Long id,
	String email,
	String phone,
	String nickname,
	String profileImageUrl,
	int point,
	OAuthType oauthType,
	Long oauthId,
	String providerId,
	Set<Address> addresses,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	public static UserResponse from(UserInfo userInfo) {
		return new UserResponse(
			userInfo.id(),
			userInfo.email(),
			userInfo.phone(),
			userInfo.nickname(),
			userInfo.profileImageUrl(),
			userInfo.point(),
			userInfo.oauthType(),
			userInfo.oauthId(),
			userInfo.providerId(),
			userInfo.addresses(),
			userInfo.createdAt(),
			userInfo.modifiedAt()
		);
	}
}
