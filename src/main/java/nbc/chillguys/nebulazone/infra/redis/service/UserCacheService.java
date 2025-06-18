package nbc.chillguys.nebulazone.infra.redis.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserCacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final UserRepository userRepository;

	private static final String USER_CACHE_PREFIX = "user:";

	public User getUserById(Long userId, long ttl) {
		String key = USER_CACHE_PREFIX + userId;

		User user = (User)redisTemplate.opsForValue().get(key);
		if (user != null) {
			return user;
		}

		user = userRepository.findActiveUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
		redisTemplate.opsForValue().set(key, user, ttl, TimeUnit.SECONDS);

		return user;
	}
}
