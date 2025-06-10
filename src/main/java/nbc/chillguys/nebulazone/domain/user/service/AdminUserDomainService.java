package nbc.chillguys.nebulazone.domain.user.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.AdminUserUpdateRequest;
import nbc.chillguys.nebulazone.domain.user.dto.AdminUserInfo;
import nbc.chillguys.nebulazone.domain.user.dto.AdminUserSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserDomainService {

	private final UserRepository userRepository;

	public Page<AdminUserInfo> findUsers(AdminUserSearchQueryCommand query, Pageable pageable) {
		return userRepository.searchUsers(query, pageable)
			.map(AdminUserInfo::from);
	}

	public User findActiveUserById(Long userId) {
		return userRepository.findActiveUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	public void updateUserStatus(Long userId, UserStatus status) {

		User user = findActiveUserById(userId);

		user.changeStatus(status);
	}

	@Transactional
	public void updateUserRoles(Long userId, Set<UserRole> roles) {
		User user = findActiveUserById(userId);

		user.updateRoles(roles);
	}

	public void updateUser(Long userId, AdminUserUpdateRequest request) {
		User user = findActiveUserById(userId);

		if (request.email() != null) {
			user.updateEmail(request.email());
		}
		if (request.phone() != null) {
			user.updatePhone(request.phone());
		}
		if (request.nickname() != null) {
			user.updateNickname(request.nickname());
		}
		if (request.profileImage() != null) {
			user.updateProfileImage(request.profileImage());
		}
	}

}
