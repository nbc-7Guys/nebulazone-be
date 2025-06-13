package nbc.chillguys.nebulazone.domain.user.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

public record UserAdminInfo(
	Long userId,
	String email,
	String phone,
	String nickname,
	Set<UserRole> roles,
	UserStatus status,
	long point,
	OAuthType oAuthType,
	String oAuthId,
	Set<UserResponse.AddressResponse> addresses,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public static UserAdminInfo from(User user) {
		return new UserAdminInfo(
			user.getId(),
			user.getEmail(),
			user.getPhone(),
			user.getNickname(),
			user.getRoles(),
			user.getStatus(),
			user.getPoint(),
			user.getOAuthType(),
			user.getOAuthId(),
			user.getAddresses().stream()
				.map(UserResponse.AddressResponse::from)
				.collect(Collectors.toSet()),
			user.getCreatedAt(),
			user.getModifiedAt()
		);
	}

}
