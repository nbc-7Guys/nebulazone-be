package nbc.chillguys.nebulazone.infra.redis.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record UserDto(
	Long id,

	String email,

	String password,

	String phone,

	String nickname,

	String profileImage,

	Long point,

	OAuthType oAuthType,

	String oAuthId,

	UserStatus status,

	Set<UserRole> roles,

	List<Address> addresses,

	LocalDateTime deletedAt
) {
	public static UserDto from(User user) {
		return new UserDto(
			user.getId(),
			user.getEmail(),
			user.getPassword(),
			user.getPhone(),
			user.getNickname(),
			user.getProfileImage(),
			user.getPoint(),
			user.getOAuthType(),
			user.getOAuthId(),
			user.getStatus(),
			user.getRoles(),
			user.getAddresses(),
			user.getDeletedAt()
		);
	}

	public static User toUser(UserDto userDto) {
		return new User(
			userDto.id,
			userDto.email,
			userDto.password,
			userDto.phone,
			userDto.nickname,
			userDto.profileImage,
			userDto.point,
			userDto.oAuthType,
			userDto.oAuthId,
			userDto.status,
			userDto.roles,
			userDto.addresses,
			userDto.deletedAt
		);
	}
}
