package nbc.chillguys.nebulazone.domain.user.dto;

import nbc.chillguys.nebulazone.application.user.dto.request.AddAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.DeleteAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateAddressUserRequest;

public record UserAddressCommand(
	String oldAddressNickname,

	String roadAddress,

	String detailAddress,

	String addressNickname
) {
	public static UserAddressCommand from(AddAddressUserRequest addAddressUserRequest) {
		return new UserAddressCommand(
			null,
			addAddressUserRequest.roadAddress(),
			addAddressUserRequest.detailAddress(),
			addAddressUserRequest.addressNickname()
		);
	}

	public static UserAddressCommand from(UpdateAddressUserRequest updateAddressUserRequest) {
		return new UserAddressCommand(
			updateAddressUserRequest.oldAddressNickname(),
			updateAddressUserRequest.roadAddress(),
			updateAddressUserRequest.detailAddress(),
			updateAddressUserRequest.addressNickname()
		);
	}

	public static UserAddressCommand from(DeleteAddressUserRequest deleteAddressUserRequest) {
		return new UserAddressCommand(
			null,
			deleteAddressUserRequest.roadAddress(),
			deleteAddressUserRequest.detailAddress(),
			deleteAddressUserRequest.addressNickname()
		);
	}
}
