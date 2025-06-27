package nbc.chillguys.nebulazone.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDomainService 테스트")
class UserAdminDomainServiceTest {

	@InjectMocks
	private UserDomainService userDomainService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Nested
	@DisplayName("findActiveUserByEmail 메서드 테스트")
	class FindActiveUserByEmailTest {

		@Test
		@DisplayName("성공 - 활성 유저 조회")
		void success_findActiveUserByEmail() {
			// Given
			String email = "test@test.com";
			User mockUser = User.builder().email(email).build();
			given(userRepository.findActiveUserByEmail(email)).willReturn(Optional.of(mockUser));

			// When
			User foundUser = userDomainService.findActiveUserByEmail(email);

			// Then
			assertThat(foundUser).isEqualTo(mockUser);
		}

		@Test
		@DisplayName("실패 - 유저를 찾을 수 없음")
		void fail_findActiveUserByEmail_userNotFound() {
			// Given
			String email = "nonexistent@test.com";
			given(userRepository.findActiveUserByEmail(email)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> userDomainService.findActiveUserByEmail(email))
				.isInstanceOf(UserException.class);
		}
	}

	@Nested
	@DisplayName("validPassword 메서드 테스트")
	class ValidPasswordTest {

		@Test
		@DisplayName("성공 - 비밀번호 일치")
		void success_validPassword() {
			// Given
			String rawPassword = "rawPassword";
			String encodedPassword = "encodedPassword";
			given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);

			// When
			userDomainService.validPassword(rawPassword, encodedPassword);

			// Then
		}

		@Test
		@DisplayName("실패 - 비밀번호 불일치")
		void fail_validPassword_wrongPassword() {
			// Given
			String rawPassword = "wrongPassword";
			String encodedPassword = "encodedPassword";
			given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(false);

			// When & Then
			assertThatThrownBy(() -> userDomainService.validPassword(rawPassword, encodedPassword))
				.isInstanceOf(UserException.class);
		}
	}

	@Nested
	@DisplayName("findActiveUserById 메서드 테스트")
	class FindActiveUserByIdTest {

		@Test
		@DisplayName("성공 - 활성 유저 조회")
		void success_findActiveUserById() {
			// Given
			Long userId = 1L;
			User mockUser = User.builder()
				.email("test@test.com")
				.password("encodedPassword")
				.phone("01012345678")
				.nickname("nickname")
				.profileImage("profile.jpg")
				.point(0L)
				.oAuthType(OAuthType.DOMAIN)
				.oAuthId(null)
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(new HashSet<>())
				.build();
			ReflectionTestUtils.setField(mockUser, "id", userId);

			given(userRepository.findActiveUserById(userId)).willReturn(Optional.of(mockUser));

			// When
			User foundUser = userDomainService.findActiveUserById(userId);

			// Then
			assertThat(foundUser).isEqualTo(mockUser);
		}

		@Test
		@DisplayName("실패 - 유저를 찾을 수 없음")
		void fail_findActiveUserById_userNotFound() {
			// Given
			Long userId = 999L;
			given(userRepository.findActiveUserById(userId)).willReturn(Optional.empty());

			// When & Then
			assertThatThrownBy(() -> userDomainService.findActiveUserById(userId))
				.isInstanceOf(UserException.class);
		}
	}

	@Nested
	@DisplayName("createUser 메서드 테스트")
	class CreateUserTest {

		@Test
		@DisplayName("성공 - 유저 생성")
		void success_createUser() {
			// Given
			UserSignUpCommand command = new UserSignUpCommand(
				"newuser@test.com",
				"password",
				"010-1234-5678",
				"New User",
				"profile.jpg",
				Set.of(Address.builder()
					.roadAddress("road")
					.detailAddress("detail")
					.addressNickname("nickname")
					.build()
				),
				OAuthType.DOMAIN,
				null
			);

			User expectedUser = User.builder()
				.email(command.email())
				.password("encodedPassword") // 암호화된 비밀번호를 예상
				.phone("01012345678") // 하이픈이 제거된 버전 예상
				.nickname(command.nickname())
				.profileImage(command.profileImageUrl())
				.point(0)
				.oAuthType(command.oAuthType())
				.oAuthId(command.oauthId())
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(command.addresses())
				.build();

			given(passwordEncoder.encode(command.password())).willReturn("encodedPassword");
			given(userRepository.save(any(User.class))).willReturn(expectedUser);

			// When
			User createdUser = userDomainService.createUser(command);

			// Then
			ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
			verify(userRepository).save(userArgumentCaptor.capture());
			User savedUser = userArgumentCaptor.getValue();

			assertThat(savedUser.getEmail()).isEqualTo(command.email());
			assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
			assertThat(savedUser.getPhone()).isEqualTo("01012345678");
			assertThat(savedUser.getNickname()).isEqualTo(command.nickname());
			assertThat(savedUser.getRoles()).contains(UserRole.ROLE_USER);
			assertThat(createdUser).isEqualTo(expectedUser);
		}
	}
}
