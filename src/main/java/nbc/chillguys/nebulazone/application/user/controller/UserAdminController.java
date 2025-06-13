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
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminSearchQuery;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRolesRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateStatusRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserAdminResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
	private final UserAdminService userAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<UserAdminResponse>> findUsers(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "role", required = false) Set<UserRole> roles,
		@RequestParam(value = "status", required = false) UserStatus status,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		UserAdminSearchQuery query = new UserAdminSearchQuery(
			keyword, status, roles, page, size
		);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<UserAdminResponse> response = userAdminService.findUsers(query, pageable);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUserDetail(@PathVariable("userId") Long userId) {
		UserResponse response = userAdminService.getUserDetail(userId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{userId}/status")
	public ResponseEntity<Void> updateUserStatus(
		@PathVariable Long userId,
		@RequestBody UserAdminUpdateStatusRequest request
	) {
		userAdminService.updateUserStatus(userId, request);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}/roles")
	public ResponseEntity<Void> updateUserRoles(
		@PathVariable Long userId,
		@RequestBody UserAdminUpdateRolesRequest request
	) {
		userAdminService.updateUserRoles(userId, request);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}")
	public ResponseEntity<Void> updateUser(
		@PathVariable Long userId,
		@RequestBody UserAdminUpdateRequest request
	) {
		userAdminService.updateUser(userId, request);
		return ResponseEntity.ok().build();
	}

}
