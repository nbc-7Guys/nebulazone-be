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

	/**
	 * 검색 조건과 페이징 정보에 따라 유저 목록을 조회합니다.
	 *
	 * @param query    유저 검색 조건
	 * @param pageable 페이징 정보
	 * @return 유저 정보 페이지
	 * @author 정석현
	 */
	public Page<UserAdminInfo> findUsers(UserAdminSearchQueryCommand query, Pageable pageable) {
		return userRepository.searchUsers(query, pageable)
			.map(UserAdminInfo::from);
	}

	/**
	 * 활성 상태의 유저를 ID로 조회합니다.<br>
	 * 유저가 없거나 비활성화된 경우 예외를 발생시킵니다.
	 *
	 * @param userId 유저 ID
	 * @return 활성 유저 엔티티
	 * @author 정석현
	 */
	public User findActiveUserById(Long userId) {
		return userRepository.findActiveUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 유저의 상태(정상, 휴면, 탈퇴 등)를 변경합니다.
	 *
	 * @param userId 유저 ID
	 * @param status 변경할 상태
	 * @author 정석현
	 */
	@Transactional
	public void updateUserStatus(Long userId, UserStatus status) {

		User user = findActiveUserById(userId);

		user.changeStatus(status);
	}

	/**
	 * 유저의 권한(역할) 정보를 변경합니다.
	 *
	 * @param userId 유저 ID
	 * @param roles  변경할 역할 목록
	 * @author 정석현
	 */
	@Transactional
	public void updateUserRoles(Long userId, Set<UserRole> roles) {
		User user = findActiveUserById(userId);

		user.updateRoles(roles);
	}

	/**
	 * 유저의 주요 정보를 개별적으로 수정합니다.<br>
	 * (이메일, 전화번호, 닉네임, 프로필 이미지 등)
	 *
	 * @param userId  유저 ID
	 * @param request 수정 요청 데이터
	 * @author 정석현
	 */
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
