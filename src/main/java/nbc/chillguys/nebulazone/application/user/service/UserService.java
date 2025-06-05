package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
	private final UserDomainService userDomainService;
	private final S3Service s3Service;

	@Transactional
	public UserResponse signUp(SignUpUserRequest signUpUserRequest, MultipartFile profileImage) {
		userDomainService.validEmail(signUpUserRequest.email());

		userDomainService.validNickname(signUpUserRequest.nickname());

		String profileImageUrl = s3Service.generateUploadUrlAndUploadFile(profileImage);

		User user = userDomainService.createUser(UserSignUpCommand.of(signUpUserRequest, profileImageUrl));

		return UserResponse.from(user);
	}

	public UserResponse getUser(Long userId) {
		User targetUser = userDomainService.findActiveUserById(userId);

		return UserResponse.from(targetUser);
	}

	@Transactional
	public UserResponse updateUser(UpdateUserRequest updateUserRequest, MultipartFile profileImage, AuthUser authUser) {
		boolean noNickname = updateUserRequest.nickname() == null;
		boolean noPassword = updateUserRequest.passwordChangeForm() == null;
		boolean noImage = (profileImage == null || profileImage.isEmpty());

		if (noNickname && noPassword && noImage) {
			throw new UserException(UserErrorCode.NOTHING_TO_UPDATE);
		}

		User user = userDomainService.findActiveUserById(authUser.getId());

		if (!noNickname) {
			userDomainService.validNickname(updateUserRequest.nickname());

			userDomainService.updateUserNickname(updateUserRequest.nickname(), user);
		}

		if (!noPassword) {
			userDomainService.validPassword(updateUserRequest.passwordChangeForm().oldPassword(), user.getPassword());

			userDomainService.updateUserPassword(updateUserRequest.passwordChangeForm().newPassword(), user);
		}

		if (!noImage) {
			s3Service.generateDeleteUrlAndDeleteFile(user.getProfileImage());

			String profileImageUrl = s3Service.generateUploadUrlAndUploadFile(profileImage);

			userDomainService.updateUserProfileImage(profileImageUrl, user);
		}

		return UserResponse.from(user);
	}

	@Transactional
	public WithdrawUserResponse withdrawUser(WithdrawUserRequest withdrawUserRequest, AuthUser authUser) {
		User user = userDomainService.findActiveUserById(authUser.getId());

		userDomainService.validPassword(withdrawUserRequest.password(), user.getPassword());

		userDomainService.withdrawUser(user);

		return WithdrawUserResponse.from(user.getId());
	}
}
