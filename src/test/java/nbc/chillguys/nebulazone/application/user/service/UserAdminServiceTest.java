package nbc.chillguys.nebulazone.application.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminSearchQuery;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRolesRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateStatusRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserAdminResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminInfo;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;
import nbc.chillguys.nebulazone.domain.user.service.UserAdminDomainService;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@InjectMocks
	private UserAdminService userAdminService;

	@Mock
	private UserAdminDomainService userAdminDomainService;

	@Nested
	@DisplayName("관리자 유저 목록 조회 테스트")
	class FindUsersTest {

		@Test
		@DisplayName("성공")
		void success_findUsers() {
			// given
			UserAdminSearchQuery request = new UserAdminSearchQuery("test", UserStatus.ACTIVE,
				Set.of(UserRole.ROLE_USER), 1, 10);
			Pageable pageable = PageRequest.of(0, 10);

			UserAdminInfo userAdminInfo = new UserAdminInfo(
				1L, "test@test.com", "01012345678", "testuser", Set.of(UserRole.ROLE_USER),
				UserStatus.ACTIVE, 0L, null, null, Set.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			Page<UserAdminInfo> infoPage = new PageImpl<>(List.of(userAdminInfo), pageable, 1);

			given(userAdminDomainService.findUsers(any(UserAdminSearchQueryCommand.class),
				any(Pageable.class))).willReturn(infoPage);

			// when
			CommonPageResponse<UserAdminResponse> response = userAdminService.findUsers(request, pageable);

			// then
			ArgumentCaptor<UserAdminSearchQueryCommand> commandCaptor = ArgumentCaptor.forClass(
				UserAdminSearchQueryCommand.class);
			then(userAdminDomainService).should(times(1)).findUsers(commandCaptor.capture(), eq(pageable));

			UserAdminSearchQueryCommand capturedCommand = commandCaptor.getValue();
			assertThat(capturedCommand.keyword()).isEqualTo(request.keyword());
			assertThat(capturedCommand.userStatus()).isEqualTo(request.status());
			assertThat(capturedCommand.roles()).isEqualTo(request.roles());

			assertThat(response).isNotNull();
			assertThat(response.content()).hasSize(1);
			assertThat(response.page()).isEqualTo(1);
			assertThat(response.totalElements()).isEqualTo(1);

			UserAdminResponse resultDto = response.content().getFirst();
			assertThat(resultDto.userId()).isEqualTo(userAdminInfo.userId());
			assertThat(resultDto.email()).isEqualTo(userAdminInfo.email());
			assertThat(resultDto.nickname()).isEqualTo(userAdminInfo.nickname());
			assertThat(resultDto.userStatus()).isEqualTo(userAdminInfo.status());
		}
	}

	@Nested
	@DisplayName("유저 상세 조회 테스트")
	class GetUserDetailTest {

		@Test
		@DisplayName("성공")
		void success_getUserDetail() {
			// given
			Long userId = 1L;
			User mockUser = User.builder()
				.email("test@test.com")
				.password("encodedPassword")
				.phone("010-1234-5678")
				.nickname("testuser")
				.profileImage("https://example.com/profile.jpg")
				.point(1000L)
				.oAuthType(OAuthType.DOMAIN)
				.roles(Set.of(UserRole.ROLE_USER))
				.addresses(Set.of(Address.builder()
					.addressNickname("집")
					.roadAddress("테스트 도로명 주소")
					.detailAddress("101호")
					.build()))
				.build();
			ReflectionTestUtils.setField(mockUser, "id", userId);

			given(userAdminDomainService.findActiveUserById(userId)).willReturn(mockUser);

			// when
			UserResponse response = userAdminService.getUserDetail(userId);

			// then
			then(userAdminDomainService).should(times(1)).findActiveUserById(userId);

			assertThat(response).isNotNull();
			assertThat(response.userId()).isEqualTo(mockUser.getId());
			assertThat(response.email()).isEqualTo(mockUser.getEmail());
			assertThat(response.phone()).isEqualTo(mockUser.getPhone());
			assertThat(response.nickname()).isEqualTo(mockUser.getNickname());
			assertThat(response.profileImageUrl()).isEqualTo(mockUser.getProfileImage());
			assertThat(response.point()).isEqualTo(mockUser.getPoint());
			assertThat(response.oAuthType()).isEqualTo(mockUser.getOAuthType());

			assertThat(response.addresses()).hasSize(1);
			UserResponse.AddressResponse addressResponse = response.addresses().iterator().next();
			Address originalAddress = mockUser.getAddresses().iterator().next();
			assertThat(addressResponse.addressNickname()).isEqualTo(originalAddress.getAddressNickname());
			assertThat(addressResponse.roadAddress()).isEqualTo(originalAddress.getRoadAddress());
			assertThat(addressResponse.detailAddress()).isEqualTo(originalAddress.getDetailAddress());
		}
	}

	@Nested
	@DisplayName("유저 상태 변경 테스트")
	class UpdateUserStatusTest {

		@Test
		@DisplayName("성공")
		void success_updateUserStatus() {
			// given
			Long userId = 1L;
			UserAdminUpdateStatusRequest request = new UserAdminUpdateStatusRequest(UserStatus.INACTIVE);

			// when
			userAdminService.updateUserStatus(userId, request);

			// then
			then(userAdminDomainService).should(times(1)).updateUserStatus(userId, request.status());
		}
	}

	@Nested
	@DisplayName("유저 권한 변경 테스트")
	class UpdateUserRolesTest {

		@Test
		@DisplayName("성공")
		void success_updateUserRoles() {
			// given
			Long userId = 1L;
			Set<UserRole> newRoles = Set.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN);
			UserAdminUpdateRolesRequest request = new UserAdminUpdateRolesRequest(newRoles);

			// when
			userAdminService.updateUserRoles(userId, request);

			// then
			then(userAdminDomainService).should(times(1)).updateUserRoles(userId, request.roles());
		}
	}

	@Nested
	@DisplayName("유저 정보 변경 테스트")
	class UpdateUserTest {

		@Test
		@DisplayName("성공")
		void success_updateUser() {
			Long userId = 1L;
			UserAdminUpdateRequest request = new UserAdminUpdateRequest("newNickname", "01012345678", "test", null);

			// when
			userAdminService.updateUser(userId, request);

			// then
			then(userAdminDomainService).should(times(1)).updateUser(userId, request);
		}
	}
}
