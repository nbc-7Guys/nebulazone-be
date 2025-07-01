package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record UserVo(
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
	public static UserVo from(User user) {
		return new UserVo(
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

	public static User toUser(UserVo userVo) {
		return new User(
			userVo.id,
			userVo.email,
			userVo.password,
			userVo.phone,
			userVo.nickname,
			userVo.profileImage,
			userVo.point,
			userVo.oAuthType,
			userVo.oAuthId,
			userVo.status,
			userVo.roles,
			userVo.addresses,
			userVo.deletedAt
		);
	}
}
