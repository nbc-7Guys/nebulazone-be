package nbc.chillguys.nebulazone.application.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@DisplayName("유저 어플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserDomainService userDomainService;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private UserService userService;

	@Spy
	private User user;

	private AuthUser authUser;

	private final MultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg",
		"content".getBytes());

	@BeforeEach
	void init() {
		user = User.builder()
			.email("test@test.com")
			.password("encodedPassword")
			.phone("01012345678")
			.nickname("test")
			.profileImage("test_profile_image_url")
			.point(0)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(Set.of(Address.builder()
				.addressNickname("test_address_nickname")
				.roadAddress("test_road_address")
				.detailAddress("test_detail_address")
				.build()))
			.build();

		authUser = AuthUser.builder()
			.id(1L)
			.email("test@test.com")
			.roles(Set.of(UserRole.ROLE_USER))
			.build();

		ReflectionTestUtils.setField(user, "id", 1L);
	}

	@Nested
	@DisplayName("회원 가입 테스트")
	class SignUpTest {
		@Test
		@DisplayName("회원 가입 성공")
		void success_signUp() {
			// Given
			SignUpUserRequest signUpUserRequest = new SignUpUserRequest("test@test.com", "testPassword1!",
				"01012345678", "test",
				Set.of(new SignUpUserRequest.SignUpUserAddressRequest("test_road_address", "test_detail_address",
					"test_address_nickname")));
			MultipartFile profileImage = mock(MultipartFile.class);

			given(s3Service.generateUploadUrlAndUploadFile(any()))
				.willReturn("test_profile_image_url");
			given(userDomainService.createUser(any()))
				.willReturn(user);

			// When
			UserResponse response = userService.signUp(signUpUserRequest, profileImage);

			// Then
			verify(userDomainService, times(1)).validEmail(anyString());
			verify(userDomainService, times(1)).validNickname(anyString());
			verify(s3Service, times(1)).generateUploadUrlAndUploadFile(any());
			verify(userDomainService, times(1)).createUser(any());

			assertThat(response)
				.isNotNull();
			assertThat(response.userId())
				.isEqualTo(1L);
			assertThat(response.email())
				.isEqualTo("test@test.com");
			assertThat(response.phone())
				.isEqualTo("01012345678");
			assertThat(response.nickname())
				.isEqualTo("test");
			assertThat(response.profileImageUrl())
				.isEqualTo("test_profile_image_url");
			assertThat(response.point())
				.isEqualTo(0);
			assertThat(response.oAuthType())
				.isEqualTo(OAuthType.DOMAIN);
			assertThat(response.addresses().size())
				.isEqualTo(1);
			assertThat(response.addresses().iterator().next().addressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(response.addresses().iterator().next().roadAddress())
				.isEqualTo("test_road_address");
			assertThat(response.addresses().iterator().next().detailAddress())
				.isEqualTo("test_detail_address");

		}
	}

	@Nested
	@DisplayName("유저 조회 테스트")
	class GetUserTest {
		@Test
		@DisplayName("유저 조회 성공")
		void success_getUser() {
			// Given
			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);

			// When
			UserResponse response = userService.getUser(1L);

			// Then
			verify(userDomainService, times(1)).findActiveUserById(anyLong());

			assertThat(response)
				.isNotNull();
			assertThat(response.userId())
				.isEqualTo(1L);
			assertThat(response.email())
				.isEqualTo("test@test.com");
			assertThat(response.phone())
				.isEqualTo("01012345678");
			assertThat(response.nickname())
				.isEqualTo("test");
			assertThat(response.profileImageUrl())
				.isEqualTo("test_profile_image_url");
			assertThat(response.point())
				.isEqualTo(0);
			assertThat(response.oAuthType())
				.isEqualTo(OAuthType.DOMAIN);
			assertThat(response.addresses().size())
				.isEqualTo(1);
			assertThat(response.addresses().iterator().next().addressNickname())
				.isEqualTo("test_address_nickname");
			assertThat(response.addresses().iterator().next().roadAddress())
				.isEqualTo("test_road_address");
			assertThat(response.addresses().iterator().next().detailAddress())
				.isEqualTo("test_detail_address");

		}
	}

	@Nested
	@DisplayName("유저 수정 테스트")
	class UpdateUserTest {
		@Test
		@DisplayName("유저 수정 성공 - 닉네임")
		void success_updateUser_nickname() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest("new_test", null);

			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);

			// When
			UserResponse response = userService.updateUser(request, null, authUser);

			// Then
			verify(userDomainService).findActiveUserById(anyLong());
			verify(userDomainService).validNickname("new_test");
			verify(userDomainService).updateUserNickname("new_test", user);

			assertThat(response)
				.isNotNull();
		}

		@Test
		@DisplayName("유저 수정 성공 - 비밀번호")
		void success_updateUser_password() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest(null,
				new UpdateUserRequest.PasswordChangeForm("encodedPassword", "newPassword"));

			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);

			// When
			UserResponse response = userService.updateUser(request, null, authUser);

			// Then
			verify(userDomainService).findActiveUserById(anyLong());
			verify(userDomainService).validPassword("encodedPassword", "encodedPassword");
			verify(userDomainService).updateUserPassword("newPassword", user);

			assertThat(response)
				.isNotNull();
		}

		@Test
		@DisplayName("유저 수정 성공 - 프로필 이미지")
		void success_updateUser_profileImage() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest(null, null);

			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);
			given(s3Service.generateUploadUrlAndUploadFile(any()))
				.willReturn("new_image_url");

			// When
			UserResponse response = userService.updateUser(request, mockImage, authUser);

			// Then
			verify(userDomainService).findActiveUserById(anyLong());
			verify(s3Service).generateDeleteUrlAndDeleteFile("test_profile_image_url");
			verify(s3Service).generateUploadUrlAndUploadFile(mockImage);
			verify(userDomainService).updateUserProfileImage("new_image_url", user);

			assertThat(response)
				.isNotNull();
		}

		@Test
		@DisplayName("전체 필드 업데이트 성공")
		void updateAllFields_success() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest("newNickname",
				new UpdateUserRequest.PasswordChangeForm("encodedPassword", "newPassword"));
			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);
			given(s3Service.generateUploadUrlAndUploadFile(any()))
				.willReturn("new_image_url");

			// When
			UserResponse response = userService.updateUser(request, mockImage, authUser);

			// Then
			verify(userDomainService).findActiveUserById(anyLong());
			verify(userDomainService).validNickname("newNickname");
			verify(userDomainService).updateUserNickname("newNickname", user);
			verify(userDomainService).validPassword("encodedPassword", "encodedPassword");
			verify(userDomainService).updateUserPassword("newPassword", user);
			verify(s3Service).generateUploadUrlAndUploadFile(mockImage);

			assertThat(response)
				.isNotNull();
		}

		@Test
		@DisplayName("유저 수정 실패 - 수정 사항이 없음")
		void fail_updateUser_nothingToUpdate() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest(null, null);

			// When
			UserException exception = assertThrows(UserException.class,
				() -> userService.updateUser(request, null, authUser));

			// Then
			assertThat(exception.getErrorCode())
				.isEqualTo(UserErrorCode.NOTHING_TO_UPDATE);

		}
	}

	@Nested
	@DisplayName("회원 탈퇴 테스트")
	class WithdrawUserTest {
		@Test
		@DisplayName("유저 탈퇴 성공")
		void success_withdrawUser() {
			// Given
			WithdrawUserRequest withdrawUserRequest = new WithdrawUserRequest("encodedPassword");

			given(userDomainService.findActiveUserById(anyLong()))
				.willReturn(user);

			// When
			Long withdrawnUserId = userService.withdrawUser(withdrawUserRequest, authUser);

			// Then
			verify(userDomainService).findActiveUserById(anyLong());
			verify(userDomainService).validPassword("encodedPassword", "encodedPassword");
			verify(userDomainService).withdrawUser(user);

			assertThat(withdrawnUserId)
				.isEqualTo(1L);

		}
	}
}
