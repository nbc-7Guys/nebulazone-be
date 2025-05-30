package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserDomainService userDomainService;

	@Transactional
	public UserResponse signUp(SignUpUserRequest signUpUserRequest) {
		userDomainService.validEmail(signUpUserRequest.email());

		userDomainService.validNickname(signUpUserRequest.nickname());

		User user = userDomainService.createUser(UserSignUpCommand.from(signUpUserRequest));

		return UserResponse.from(user);
	}
}
