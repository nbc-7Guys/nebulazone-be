package nbc.chillguys.nebulazone.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.dto.UserUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@DisplayName("유저 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserDomainService userDomainService;

	@Spy
	private User user;

	@BeforeEach
	void init() {
		user = User.builder()
			.email("test@test.com")
			.password("encodedPassword")
			.phone("01012345678")
			.nickname("test")
			.profileImage("test.jpg")
			.point(0)
			.oAuthType(OAuthType.KAKAO)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(Set.of(Address.builder()
				.addressNickname("test_address_nickname")
				.roadAddress("test_road_address")
				.detailAddress("test_detail_address")
				.build()))
			.build();

		ReflectionTestUtils.setField(user, "id", 1L);
	}

	@Nested
	@DisplayName("유저 생성 테스트")
	class CreateUserTest {
		@Test
		@DisplayName("유저 생성 성공")
		void success_createUser() {
			// Given
			UserSignUpCommand userSignUpCommand = new UserSignUpCommand(
				"test@test.com",
				"encodedPassword",
				"01012345678",
				"test",
				"test.jpg",
				Set.of(Address.builder()
					.addressNickname("test_address_nickname")
					.roadAddress("test_road_address")
					.detailAddress("test_detail_address")
					.build()),
				OAuthType.KAKAO,
				"test_oauth_id"
			);

			given(userRepository.save(any(User.class)))
				.willReturn(user);

			// When
			User savedUser = userDomainService.createUser(userSignUpCommand);

			// Then
			verify(userRepository, times(1)).save(any(User.class));

			assertThat(savedUser.getId())
				.isEqualTo(1L);
			assertThat(savedUser.getNickname())
				.isEqualTo("test");
			assertThat(savedUser.getAddresses().size())
				.isEqualTo(1);
			assertThat(savedUser.getAddresses().iterator().next().getAddressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(savedUser.getAddresses().iterator().next().getRoadAddress())
				.isEqualTo("test_road_address");
			assertThat(savedUser.getAddresses().iterator().next().getDetailAddress())
				.isEqualTo("test_detail_address");
			assertThat(savedUser.getRoles().size())
				.isEqualTo(1);
			assertThat(savedUser.getRoles().iterator().next().name())
				.isEqualTo("ROLE_USER");
			assertThat(savedUser.getOAuthType())
				.isEqualTo(OAuthType.KAKAO);
			assertThat(savedUser.getPoint())
				.isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("유저 조회 테스트")
	class FindActiveUserTest {
		@Test
		@DisplayName("유저 이메일 조회 성공")
		void success_findActiveUserByEmail() {
			// Given
			given(userRepository.findActiveUserByEmail(anyString()))
				.willReturn(Optional.ofNullable(user));

			// When
			User findedUser = userDomainService.findActiveUserByEmail("test@test.com");

			// Then
			verify(userRepository, times(1)).findActiveUserByEmail(anyString());

			assertThat(findedUser.getId())
				.isEqualTo(1L);
			assertThat(findedUser.getNickname())
				.isEqualTo("test");
			assertThat(findedUser.getAddresses().size())
				.isEqualTo(1);
			assertThat(findedUser.getAddresses().iterator().next().getAddressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(findedUser.getAddresses().iterator().next().getRoadAddress())
				.isEqualTo("test_road_address");
			assertThat(findedUser.getAddresses().iterator().next().getDetailAddress())
				.isEqualTo("test_detail_address");
			assertThat(findedUser.getRoles().size())
				.isEqualTo(1);
			assertThat(findedUser.getRoles().iterator().next().name())
				.isEqualTo("ROLE_USER");
			assertThat(findedUser.getOAuthType())
				.isEqualTo(OAuthType.KAKAO);
			assertThat(findedUser.getPoint())
				.isEqualTo(0);
		}

		@Test
		@DisplayName("유저 이메일 조회 실패 - 유저를 찾을 수 없음")
		void fail_findActiveUserByEmail_userNotFound() {
			// Given
			given(userRepository.findActiveUserByEmail(anyString()))
				.willReturn(Optional.empty());

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.findActiveUserByEmail("test@test.com"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

		}

		@Test
		@DisplayName("유저 id 조회 성공")
		void success_findActiveUserById() {
			// Given
			given(userRepository.findActiveUserById(anyLong()))
				.willReturn(Optional.ofNullable(user));

			// When
			User findedUser = userDomainService.findActiveUserById(1L);

			// Then
			verify(userRepository, times(1)).findActiveUserById(anyLong());

			assertThat(findedUser.getId())
				.isEqualTo(1L);
			assertThat(findedUser.getNickname())
				.isEqualTo("test");
			assertThat(findedUser.getAddresses().size())
				.isEqualTo(1);
			assertThat(findedUser.getAddresses().iterator().next().getAddressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(findedUser.getAddresses().iterator().next().getRoadAddress())
				.isEqualTo("test_road_address");
			assertThat(findedUser.getAddresses().iterator().next().getDetailAddress())
				.isEqualTo("test_detail_address");
			assertThat(findedUser.getRoles().size())
				.isEqualTo(1);
			assertThat(findedUser.getRoles().iterator().next().name())
				.isEqualTo("ROLE_USER");
			assertThat(findedUser.getOAuthType())
				.isEqualTo(OAuthType.KAKAO);
			assertThat(findedUser.getPoint())
				.isEqualTo(0);
		}

		@Test
		@DisplayName("유저 id 조회 실패 - 유저를 찾을 수 없음")
		void fail_findActiveUserById_userNotFound() {
			// Given
			given(userRepository.findActiveUserById(anyLong()))
				.willReturn(Optional.empty());

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.findActiveUserById(2L));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

		}

		@Test
		@DisplayName("유저 이메일, 소셜 로그인 타입 조회 성공")
		void success_findActiveUserByEmailAndOauthType() {
			// Given
			given(userRepository.findActiveUserByEmailAndOAuthType(anyString(), any()))
				.willReturn(Optional.ofNullable(user));

			// When
			User findedUser = userDomainService.findActiveUserByEmailAndOAuthType("test@test.com", OAuthType.KAKAO);

			// Then
			verify(userRepository, times(1)).findActiveUserByEmailAndOAuthType(anyString(), any());

			assertThat(findedUser.getId())
				.isEqualTo(1L);
			assertThat(findedUser.getNickname())
				.isEqualTo("test");
			assertThat(findedUser.getAddresses().size())
				.isEqualTo(1);
			assertThat(findedUser.getAddresses().iterator().next().getAddressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(findedUser.getAddresses().iterator().next().getRoadAddress())
				.isEqualTo("test_road_address");
			assertThat(findedUser.getAddresses().iterator().next().getDetailAddress())
				.isEqualTo("test_detail_address");
			assertThat(findedUser.getRoles().size())
				.isEqualTo(1);
			assertThat(findedUser.getRoles().iterator().next().name())
				.isEqualTo("ROLE_USER");
			assertThat(findedUser.getOAuthType())
				.isEqualTo(OAuthType.KAKAO);
			assertThat(findedUser.getPoint())
				.isEqualTo(0);
		}

		@Test
		@DisplayName("유저 이메일, 소셜 로그인 타입 조회 실패 - 이미 존재하는 이메일")
		void fail_findActiveUserByEmailAndOAuthType_alreadyExistsEmail() {
			// Given
			given(userRepository.findActiveUserByEmailAndOAuthType(anyString(), any()))
				.willReturn(Optional.empty());

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.findActiveUserByEmailAndOAuthType("test@test.com", OAuthType.KAKAO));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.ALREADY_EXISTS_EMAIL);

		}
	}

	@Nested
	@DisplayName("검증 테스트")
	class ValidTest {
		@Test
		@DisplayName("비밀번호 검증 실패 - 비밀번호 오류")
		void fail_validPassword_wrongPassword() {
			// Given

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.validPassword("wrongPassword", "encodedPassword"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.WRONG_PASSWORD);

		}

		@Test
		@DisplayName("비밀번호 검증 실패 - 동일한 비밀번호")
		void fail_validNewPassword_samePassword() {
			// Given
			given(passwordEncoder.matches("newPassword", "encodedPassword"))
				.willReturn(true);

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.validNewPassword("newPassword", "encodedPassword"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.SAME_PASSWORD);

		}

		@Test
		@DisplayName("이메일 검증 실패 - 이미 존재하는 이메일")
		void fail_validEmail_alreadyExistsEmail() {
			// Given
			given(userRepository.existsByEmail("test@test.com"))
				.willReturn(true);

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.validEmail("test@test.com"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.ALREADY_EXISTS_EMAIL);

		}

		@Test
		@DisplayName("닉네임 검증 실패 - 이미 존재하는 닉네임")
		void fail_validNickname_alreadyExistsNickname() {
			// Given
			given(userRepository.existsByNickname("test"))
				.willReturn(true);

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.validNickname("test"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.ALREADY_EXISTS_NICKNAME);

		}

		@Test
		@DisplayName("전화번호 검증 실패 - 이미 존재하는 전화번호")
		void fail_validPhone_alreadyExistsPhone() {
			// Given
			given(userRepository.existsByPhone("01012345678"))
				.willReturn(true);

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userDomainService.validPhone("01012345678"));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.ALREADY_EXISTS_PHONE);

		}
	}

	@Nested
	@DisplayName("유저 수정 테스트")
	class UpdateUserTest {
		@Test
		@DisplayName("유저 닉네임 변경 성공")
		void success_updateUserNicknameOrPassword_nickname() {
			// Given
			UserUpdateCommand userUpdateCommand = new UserUpdateCommand(user, "newTest", null, null);
			String originalNickname = user.getNickname();

			// When
			userDomainService.updateUserNicknameOrPassword(userUpdateCommand);

			// Then
			assertThat(user.getNickname())
				.isNotEqualTo(originalNickname)
				.isEqualTo("newTest");

		}

		@Test
		@DisplayName("유저 비밀번호 변경 성공")
		void success_updateUserNicknameOrPassword_password() {
			// Given
			UserUpdateCommand userUpdateCommand = new UserUpdateCommand(user, null, "encodedPassword", "newPassword");
			String originalPassword = user.getPassword();

			given(passwordEncoder.matches("encodedPassword", user.getPassword()))
				.willReturn(true);
			given(passwordEncoder.encode("newPassword"))
				.willReturn("newEncodedPassword");

			// When
			userDomainService.updateUserNicknameOrPassword(userUpdateCommand);

			// Then
			assertThat(user.getPassword())
				.isNotEqualTo(originalPassword)
				.isEqualTo("newEncodedPassword");

		}

		@Test
		@DisplayName("유저 비밀번호, 닉네임 변경 성공")
		void success_updateUserNicknameOrPassword_all() {
			// Given
			UserUpdateCommand userUpdateCommand = new UserUpdateCommand(user, "newTest", "encodedPassword",
				"newPassword");
			String originalNickname = user.getNickname();
			String originalPassword = user.getPassword();

			given(passwordEncoder.matches("encodedPassword", user.getPassword()))
				.willReturn(true);
			given(passwordEncoder.encode("newPassword"))
				.willReturn("newEncodedPassword");

			// When
			userDomainService.updateUserNicknameOrPassword(userUpdateCommand);

			// Then
			assertThat(user.getPassword())
				.isNotEqualTo(originalPassword)
				.isEqualTo("newEncodedPassword");

			assertThat(user.getNickname())
				.isNotEqualTo(originalNickname)
				.isEqualTo("newTest");

		}

		@Test
		@DisplayName("유저 닉네임, 비밀번호 수정 실패 - 수정 사항이 존재하지 않음")
		void fail_updateUserNicknameOrPassword_nothingToUpdate() {
			// Given
			UserUpdateCommand userUpdateCommand = new UserUpdateCommand(user, null, null, null);

			// When
			UserException exception = assertThrows(UserException.class, () ->
				userDomainService.updateUserNicknameOrPassword(userUpdateCommand));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.NOTHING_TO_UPDATE);

		}

		@Test
		@DisplayName("유저 프로필 이미지 수정 성공")
		void success_updateUserProfileImage() {
			// Given
			String originalProfileImage = user.getProfileImage();

			// When
			userDomainService.updateUserProfileImage("new_Test.jpg", user);

			// Then
			assertThat(user.getProfileImage())
				.isNotEqualTo(originalProfileImage)
				.isEqualTo("new_Test.jpg");

		}
	}

	@Nested
	@DisplayName("유저 탈퇴 테스트")
	class WithdrawUserTest {
		@Test
		@DisplayName("유저 탈퇴 성공")
		void success_withdrawUser() {
			// Given
			UserStatus originalStatus = user.getStatus();
			LocalDateTime originalDeletedAt = user.getDeletedAt();

			// When
			userDomainService.withdrawUser(user);

			// Then
			assertThat(user.getStatus())
				.isNotEqualTo(originalStatus)
				.isEqualTo(UserStatus.INACTIVE);
			assertThat(user.getDeletedAt())
				.isNotEqualTo(originalDeletedAt)
				.isNotNull();

		}
	}

}
