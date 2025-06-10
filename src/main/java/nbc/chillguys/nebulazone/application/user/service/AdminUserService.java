package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.AdminUserSearchQuery;
import nbc.chillguys.nebulazone.application.user.dto.response.AdminUserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.dto.AdminUserInfo;
import nbc.chillguys.nebulazone.domain.user.dto.AdminUserSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.AdminUserDomainService;

@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final AdminUserDomainService adminUserDomainService;

	public CommonPageResponse<AdminUserResponse> findUsers(AdminUserSearchQuery request, Pageable pageable) {
		AdminUserSearchQueryCommand command = new AdminUserSearchQueryCommand(
			request.keyword(),
			request.status(),
			request.roles()

		);

		Page<AdminUserInfo> infoPage = adminUserDomainService.findUsers(command, pageable);

		// Page<AdminUserResponse>
		Page<AdminUserResponse> responsePage = infoPage.map(AdminUserResponse::from);

		// 래핑
		return CommonPageResponse.from(responsePage);
	}

	public UserResponse getUserDetail(Long userId) {
		User targetUser = adminUserDomainService.findActiveUserById(userId);

		return UserResponse.from(targetUser);
	}
}
