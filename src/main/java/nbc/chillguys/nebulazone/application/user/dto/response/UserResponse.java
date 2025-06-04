package nbc.chillguys.nebulazone.application.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
	Long userId,
	String email,
	String phone,
	String nickname,
	String profileImageUrl,
	int point,
	OAuthType oauthType,
	String oauthId,
	Set<AddressResponse> addresses,
	LocalDateTime createdAt,
	LocalDateTime modifiedAt
) {
	@Builder
	public record AddressResponse(
		String roadAddress,
		String detailAddress,
		String addressNickname
	) {
		public static AddressResponse from(Address address) {
			return AddressResponse.builder()
				.roadAddress(address.getRoadAddress())
				.detailAddress(address.getDetailAddress())
				.addressNickname(address.getAddressNickname())
				.build();
		}
	}

	public static UserResponse from(User user) {
		return UserResponse.builder()
			.userId(user.getId())
			.email(user.getEmail())
			.phone(user.getPhone())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImage())
			.point(user.getPoint())
			.oauthType(user.getOauthType())
			.oauthId(user.getOauthId())
			.addresses(user.getAddresses().stream()
				.map(AddressResponse::from)
				.collect(Collectors.toSet()))
			.createdAt(user.getCreatedAt())
			.modifiedAt(user.getModifiedAt())
			.build();
	}
}
