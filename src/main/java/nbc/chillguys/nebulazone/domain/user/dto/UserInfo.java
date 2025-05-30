package nbc.chillguys.nebulazone.domain.user.dto;

import java.time.LocalDateTime;
import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record UserInfo(
	Long id,
	String email,
	String password,
	String phone,
	String nickname,
	String profileImageUrl,
	int point,
	OAuthType oauthType,
	Long oauthId,
	String providerId,
	Set<Address> addresses,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt,
	LocalDateTime deletedAt
) {
	public static UserInfo from(User user) {
		return new UserInfo(
			user.getId(),
			user.getEmail(),
			user.getPassword(),
			user.getPhone(),
			user.getNickname(),
			user.getProfileImage(),
			user.getPoint(),
			user.getOauthType(),
			user.getOauthId(),
			user.getProviderId(),
			user.getAddresses(),
			user.getCreatedAt(),
			user.getModifiedAt(),
			user.getDeletedAt()
		);
	}
}
