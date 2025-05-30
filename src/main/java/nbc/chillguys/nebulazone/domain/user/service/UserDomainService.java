package nbc.chillguys.nebulazone.domain.user.service;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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

	/**
	 * 탈퇴되지 않은 유저를 email로 조회<br>
	 * 조회 시 roles도 함께 fetch
	 * @param email 이메일
	 * @throws UserException 없을 시 예외 발생
	 * @return id, email, password, roles
	 * @author 이승현
	 */
	public User findActiveUserByEmail(String email) {
		return userRepository.findActiveUserByEmail(email)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 비밀번호 검증
	 * @param rawPassword 원본 비밀번호
	 * @param encodedPassword 암호화된 비밀번호
	 * @throws UserException 일치 하지 않을 시 예외 발생
	 */
	public void validPassword(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new UserException(UserErrorCode.WRONG_PASSWORD);
		}
	}

	/**
	 * 탈퇴되지 않은 유저를 id로 조회<br>
	 * 조회 시 address도 함께 fetch
	 * @param userId 유저 id
	 * @throws UserException 없을 시 예외 발생
	 * @return 유저의 모든 정보
	 * @author 이승현
	 */
	public User findActiveUserById(Long userId) {
		return userRepository.findActiveUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 유저 생성<br>
	 * domain 가입 유형
	 * @param userSignUpCommand 유저 가입 정보
	 * @return 유저의 모든 정보
	 * @author 이승현
	 */
	public User createUser(UserSignUpCommand userSignUpCommand) {
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

		return userRepository.save(user);
	}

	/**
	 * 이메일 검증
	 * @param email 이메일
	 * @throws UserException 중복된 이메일이 존재할 시 예외 발생
	 */
	public void validEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new UserException(UserErrorCode.ALREADY_EXISTS_EMAIL);
		}
	}

	/**
	 * 닉네임 검증
	 * @param nickname 닉네임
	 * @throws UserException 중복된 닉네임 존재할 시 예외 발생
	 */
	public void validNickname(String nickname) {
		if (userRepository.existsByNickname(nickname)) {
			throw new UserException(UserErrorCode.ALREADY_EXISTS_NICKNAME);
		}
	}
}
