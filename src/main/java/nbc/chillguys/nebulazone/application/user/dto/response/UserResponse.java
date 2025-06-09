package nbc.chillguys.nebulazone.application.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
	Long userId,
	String email,
	String phone,
	String nickname,
	String profileImageUrl,
	int point,
	OAuthType oAuthType,
	String oAuthId,
	Set<AddressResponse> addresses,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime modifiedAt
) {
	public record AddressResponse(
		String roadAddress,
		String detailAddress,
		String addressNickname
	) {
		public static AddressResponse from(Address address) {
			return new AddressResponse(
				address.getRoadAddress(),
				address.getDetailAddress(),
				address.getAddressNickname()
			);
		}
	}

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getPhone(),
			user.getNickname(),
			user.getProfileImage(),
			user.getPoint(),
			user.getOAuthType(),
			user.getOAuthId(),
			user.getAddresses().stream()
				.map(AddressResponse::from)
				.collect(Collectors.toSet()),
			user.getCreatedAt(),
			user.getModifiedAt()
		);
	}
}
