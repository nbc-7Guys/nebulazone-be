package nbc.chillguys.nebulazone.domain.user.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.dto.UserInfo;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignInInfo;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDomainService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserSignInInfo findActiveUserByEmail(String email) {
		User user = userRepository.findActiveUserByEmail(email)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		return UserSignInInfo.from(user);
	}

	public void validPassword(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new UserException(UserErrorCode.WRONG_PASSWORD);
		}
	}

	public UserInfo findActiveUserById(Long userId) {
		User user = userRepository.findActiveUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		return UserInfo.from(user);
	}

	public UserInfo createUser(UserSignUpCommand userSignUpCommand) {
		User user = User.builder()
			.email(userSignUpCommand.email())
			.password(passwordEncoder.encode(userSignUpCommand.password()))
			.phone(userSignUpCommand.phone())
			.nickname(userSignUpCommand.nickname())
			.profileImage(userSignUpCommand.profileImageUrl())
			.point(0)
			.oauthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(userSignUpCommand.addresses())
			.build();

		User savedUser = userRepository.save(user);

		return UserInfo.from(savedUser);
	}

	public void validEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
		}
	}

	public void validNickname(String nickname) {
		if (userRepository.existsByNickname(nickname)) {
			throw new UserException(UserErrorCode.ALREADY_EXISTS_NICKNAME);
		}
	}
}
