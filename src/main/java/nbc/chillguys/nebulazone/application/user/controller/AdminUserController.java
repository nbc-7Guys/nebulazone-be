package nbc.chillguys.nebulazone.application.user.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.AdminUserSearchQuery;
import nbc.chillguys.nebulazone.application.user.dto.request.AdminUserUpdateRolesRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.AdminUserUpdateStatusRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.AdminUserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.service.AdminUserService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
	private final AdminUserService adminUserService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminUserResponse>> findUsers(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "role", required = false) Set<UserRole> roles,
		@RequestParam(value = "status", required = false) UserStatus status,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminUserSearchQuery query = new AdminUserSearchQuery(
			keyword, status, roles, page, size
		);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminUserResponse> response = adminUserService.findUsers(query, pageable);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserDetail(@PathVariable("userId") Long userId) {
		UserResponse response = adminUserService.getUserDetail(userId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/admin/users/{userId}/status")
	public ResponseEntity<Void> updateUserStatus(
		@PathVariable Long userId,
		@RequestBody AdminUserUpdateStatusRequest request
	) {
		adminUserService.updateUserStatus(userId, request);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/admin/users/{userId}/roles")
	public ResponseEntity<Void> updateUserRoles(
		@PathVariable Long userId,
		@RequestBody AdminUserUpdateRolesRequest request
	) {
		adminUserService.updateUserRoles(userId, request);
		return ResponseEntity.ok().build();
	}

}
