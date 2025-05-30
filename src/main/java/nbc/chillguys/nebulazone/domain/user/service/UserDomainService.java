package nbc.chillguys.nebulazone.domain.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDomainService {

	private final UserRepository userRepository;

	/**
	 * 유저 조회 메서드
	 */
	public User getUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
	}
}
