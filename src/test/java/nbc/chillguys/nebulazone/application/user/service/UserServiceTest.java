package nbc.chillguys.nebulazone.application.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
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

import nbc.chillguys.nebulazone.application.user.dto.request.AddAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.DeleteAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@DisplayName("유저 어플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserDomainService userDomainService;

	@Mock
	private GcsClient gcsClient;

	@Mock
	private UserCacheService userCacheService;

	@InjectMocks
	private UserService userService;

	@Spy
	private User user;

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
			.addresses(List.of(Address.builder()
				.addressNickname("test_address_nickname")
				.roadAddress("test_road_address")
				.detailAddress("test_detail_address")
				.build()))
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
				List.of(new SignUpUserRequest.SignUpUserAddressRequest("test_road_address", "test_detail_address",
					"test_address_nickname")));

			given(userDomainService.createUser(any()))
				.willReturn(user);

			// When
			UserResponse response = userService.signUp(signUpUserRequest);

			// Then
			verify(userDomainService, times(1)).validEmail(anyString());
			verify(userDomainService, times(1)).validNickname(anyString());
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
	@DisplayName("유저 수정 테스트")
	class UpdateUserTest {
		@Test
		@DisplayName("유저 수정 성공 - 프로필 이미지")
		void success_updateUserProfileImage() {
			// Given
			MultipartFile mockImage = new MockMultipartFile("image", "test.jpg", "image/jpeg",
				"content".getBytes());

			given(userDomainService.updateUserProfileImage(anyString(), any()))
				.willReturn(user);
			given(gcsClient.uploadFile(any()))
				.willReturn("new_image_url");

			// When
			UserResponse response = userService.updateUserProfileImage(mockImage, user);

			// Then
			verify(userCacheService).deleteUserById(user.getId());
			verify(gcsClient).deleteFile("test_profile_image_url");
			verify(gcsClient).uploadFile(mockImage);
			verify(userDomainService).updateUserProfileImage("new_image_url", user);

			assertThat(response)
				.isNotNull();
		}

		@Test
		@DisplayName("유저 닉네임, 비밀번호 수정 성공")
		void success_userNicknameOrPassword() {
			// Given
			UpdateUserRequest request = new UpdateUserRequest("newNickname",
				new UpdateUserRequest.PasswordChangeForm("encodedPassword", "newPassword"));

			given(userDomainService.updateUserNicknameOrPassword(any()))
				.willReturn(user);

			// When
			UserResponse response = userService.updateUserNicknameOrPassword(request, user);

			// Then
			verify(userCacheService).deleteUserById(user.getId());
			verify(userDomainService).updateUserNicknameOrPassword(any());

			assertThat(response)
				.isNotNull();
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

			given(userDomainService.withdrawUser(any()))
				.willReturn(user);

			// When
			WithdrawUserResponse response = userService.withdrawUser(withdrawUserRequest, user);

			// Then
			verify(userDomainService, times(1)).withdrawUser(any());
			verify(userCacheService).deleteUserById(user.getId());
			verify(userDomainService).validPassword("encodedPassword", "encodedPassword");
			verify(userDomainService).withdrawUser(user);

			assertThat(response.userId())
				.isEqualTo(1L);

		}
	}

	@Nested
	@DisplayName("유저 주소 관리 테스트")
	class UserAddressTest {

		@Test
		@DisplayName("주소 추가 성공")
		void success_addAddress() {
			// Given
			AddAddressUserRequest addRequest = new AddAddressUserRequest(
				"test_road_address",
				"test_detail_address",
				"test_address_nickname"
			);

			Address address = Address.builder()
				.roadAddress("test_road_address")
				.detailAddress("test_detail_address")
				.addressNickname("test_address_nickname")
				.build();

			User updatedUser = User.builder()
				.email("test@test.com")
				.password("encodedPassword")
				.phone("01012345678")
				.nickname("test")
				.profileImage("test_profile_image_url")
				.point(0)
				.oAuthType(OAuthType.DOMAIN)
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(List.of(address))
				.build();
			ReflectionTestUtils.setField(updatedUser, "id", 1L);

			given(userDomainService.addAddress(any(User.class), any()))
				.willReturn(updatedUser);

			// When
			UserResponse response = userService.addAddress(addRequest, user);

			// Then
			verify(userDomainService, times(1)).addAddress(any(User.class), any());
			verify(userCacheService, times(1)).deleteUserById(updatedUser.getId());

			assertThat(response).isNotNull();
			assertThat(response.addresses().size()).isEqualTo(1);
			UserResponse.AddressResponse addrResp = response.addresses().iterator().next();
			assertThat(addrResp.addressNickname()).isEqualTo("test_address_nickname");
			assertThat(addrResp.roadAddress()).isEqualTo("test_road_address");
			assertThat(addrResp.detailAddress()).isEqualTo("test_detail_address");
		}

		@Test
		@DisplayName("주소 수정 성공")
		void success_updateAddress() {
			// Given
			UpdateAddressUserRequest updateRequest = new UpdateAddressUserRequest(
				"old_address_nickname",
				"new_road_address",
				"new_detail_address",
				"new_address_nickname"
			);

			Address updatedAddress = Address.builder()
				.roadAddress("new_road_address")
				.detailAddress("new_detail_address")
				.addressNickname("new_address_nickname")
				.build();

			User updatedUser = User.builder()
				.email("test@test.com")
				.password("encodedPassword")
				.phone("01012345678")
				.nickname("test")
				.profileImage("test_profile_image_url")
				.point(0)
				.oAuthType(OAuthType.DOMAIN)
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(List.of(updatedAddress))
				.build();
			ReflectionTestUtils.setField(updatedUser, "id", 1L);

			given(userDomainService.updateAddress(any(User.class), any()))
				.willReturn(updatedUser);

			// When
			UserResponse response = userService.updateAddress(updateRequest, user);

			// Then
			verify(userDomainService, times(1)).updateAddress(any(User.class), any());
			verify(userCacheService, times(1)).deleteUserById(updatedUser.getId());

			assertThat(response).isNotNull();
			assertThat(response.addresses().size()).isEqualTo(1);
			UserResponse.AddressResponse addrResp = response.addresses().iterator().next();
			assertThat(addrResp.addressNickname()).isEqualTo("new_address_nickname");
			assertThat(addrResp.roadAddress()).isEqualTo("new_road_address");
			assertThat(addrResp.detailAddress()).isEqualTo("new_detail_address");
		}

		@Test
		@DisplayName("주소 삭제 성공")
		void success_deleteAddress() {
			// Given
			DeleteAddressUserRequest deleteRequest = new DeleteAddressUserRequest(
				"test_road_address",
				"test_detail_address",
				"test_address_nickname"
			);

			User updatedUser = User.builder()
				.email("test@test.com")
				.password("encodedPassword")
				.phone("01012345678")
				.nickname("test")
				.profileImage("test_profile_image_url")
				.point(0)
				.oAuthType(OAuthType.DOMAIN)
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(List.of())
				.build();
			ReflectionTestUtils.setField(updatedUser, "id", 1L);

			given(userDomainService.removeAddress(any(User.class), any()))
				.willReturn(updatedUser);

			// When
			UserResponse response = userService.deleteAddress(deleteRequest, user);

			// Then
			verify(userDomainService, times(1)).removeAddress(any(User.class), any());
			verify(userCacheService, times(1)).deleteUserById(updatedUser.getId());

			assertThat(response).isNotNull();
			assertThat(response.addresses().size()).isEqualTo(0);
		}
	}

}
