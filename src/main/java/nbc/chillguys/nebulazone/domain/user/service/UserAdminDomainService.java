package nbc.chillguys.nebulazone.domain.user.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.UserAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminInfo;
import nbc.chillguys.nebulazone.domain.user.dto.UserAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.entity.UserStatus;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserAdminDomainService {

	private final UserRepository userRepository;

	public Page<UserAdminInfo> findUsers(UserAdminSearchQueryCommand query, Pageable pageable) {
		return userRepository.searchUsers(query, pageable)
			.map(UserAdminInfo::from);
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

	public void updateUser(Long userId, UserAdminUpdateRequest request) {
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
