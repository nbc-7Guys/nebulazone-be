package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminSearchQuery;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRolesRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateStatusRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserAdminResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminInfo;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserAdminDomainService;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@Service
@RequiredArgsConstructor
public class UserAdminService {

	private final UserAdminDomainService userAdminDomainService;
	private final UserCacheService userCacheService;

	@Transactional(readOnly = true)
	public CommonPageResponse<UserAdminResponse> findUsers(UserAdminSearchQuery request, Pageable pageable) {
		UserAdminSearchQueryCommand command = new UserAdminSearchQueryCommand(
			request.keyword(),
			request.status(),
			request.roles()

		);

		Page<UserAdminInfo> infoPage = userAdminDomainService.findUsers(command, pageable);

		Page<UserAdminResponse> responsePage = infoPage.map(UserAdminResponse::from);

		return CommonPageResponse.from(responsePage);
	}

	@Transactional(readOnly = true)
	public UserResponse getUserDetail(Long userId) {
		User targetUser = userAdminDomainService.findActiveUserById(userId);

		return UserResponse.from(targetUser);
	}

	public void updateUserStatus(Long userId, UserAdminUpdateStatusRequest request) {
		userAdminDomainService.updateUserStatus(userId, request.status());
		userCacheService.deleteUserById(userId);
	}

	public void updateUserRoles(Long userId, UserAdminUpdateRolesRequest request) {
		userAdminDomainService.updateUserRoles(userId, request.roles());
		userCacheService.deleteUserById(userId);
	}

	public void updateUser(Long userId, UserAdminUpdateRequest request) {
		userAdminDomainService.updateUser(userId, request);
		userCacheService.deleteUserById(userId);
	}

}
