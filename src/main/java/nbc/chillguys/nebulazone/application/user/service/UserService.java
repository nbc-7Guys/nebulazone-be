package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.AddAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.DeleteAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserAddressCommand;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.dto.UserUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserDomainService userDomainService;
	private final GcsClient gcsClient;
	private final UserCacheService userCacheService;

	public UserResponse signUp(SignUpUserRequest signUpUserRequest) {
		userDomainService.validEmail(signUpUserRequest.email());

		userDomainService.validNickname(signUpUserRequest.nickname());

		userDomainService.validPhone(signUpUserRequest.phone());

		User user = userDomainService.createUser(UserSignUpCommand.from(signUpUserRequest));

		return UserResponse.from(user);
	}

	public UserResponse getUser(Long userId) {
		User targetUser = userDomainService.findActiveUserById(userId);

		return UserResponse.from(targetUser);
	}

	public UserResponse updateUserNicknameOrPassword(UpdateUserRequest updateUserRequest, User loggedInUser) {
		User user = userDomainService.updateUserNicknameOrPassword(
			UserUpdateCommand.of(updateUserRequest, loggedInUser)
		);

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}

	public UserResponse updateUserProfileImage(MultipartFile profileImage, User loggedInUser) {
		if (loggedInUser.getProfileImage() != null) {
			gcsClient.deleteFile(loggedInUser.getProfileImage());
		}

		String profileImageUrl = gcsClient.uploadFile(profileImage);

		User updatedUser = userDomainService.updateUserProfileImage(profileImageUrl, loggedInUser);

		userCacheService.deleteUserById(updatedUser.getId());

		return UserResponse.from(updatedUser);
	}

	public WithdrawUserResponse withdrawUser(WithdrawUserRequest withdrawUserRequest, User loggedInUser) {
		userDomainService.validPassword(withdrawUserRequest.password(), loggedInUser.getPassword());

		User withdrawnUser = userDomainService.withdrawUser(loggedInUser);

		userCacheService.deleteUserById(withdrawnUser.getId());

		return WithdrawUserResponse.from(withdrawnUser.getId());
	}

	public UserResponse addAddress(AddAddressUserRequest addAddressUserRequest, User loggedInUser) {
		User user = userDomainService.addAddress(loggedInUser,
			UserAddressCommand.from(addAddressUserRequest));

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}

	public UserResponse updateAddress(UpdateAddressUserRequest updateAddressUserRequest, User loggedInUser) {
		User user = userDomainService.updateAddress(loggedInUser, UserAddressCommand.from(updateAddressUserRequest));

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}

	public UserResponse deleteAddress(DeleteAddressUserRequest deleteAddressUserRequest, User loggedInUser) {
		User user = userDomainService.removeAddress(loggedInUser, UserAddressCommand.from(deleteAddressUserRequest));

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}
}
