package nbc.chillguys.nebulazone.application.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import nbc.chillguys.nebulazone.domain.user.dto.AdminUserInfo;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminUserResponse(
	Long userId,
	String email,
	String phone,
	String nickname,
	int point,
	OAuthType oAuthType,
	String oAuthId,
	Set<AddressResponse> addresses,
	Set<UserRole> userRole,
	UserStatus userStatus,
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

	public static AdminUserResponse from(AdminUserInfo info) {
		return new AdminUserResponse(
			info.userId(),
			info.email(),
			info.phone(),
			info.nickname(),
			info.point(),
			info.oAuthType(),
			info.oAuthId(),
			info.addresses().stream()
				.map(address -> new AddressResponse(address.roadAddress(), address.detailAddress(),
					address.addressNickname()))
				.collect(Collectors.toSet()),
			info.roles(),
			info.status(),
			info.createdAt(),
			info.modifiedAt()
		);
	}
}
