package nbc.chillguys.nebulazone.infra.redis.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;
import nbc.chillguys.nebulazone.infra.redis.vo.UserVo;

@Service
@RequiredArgsConstructor
public class UserCacheService {

	private static final String USER_CACHE_PREFIX = "user:";
	private final RedisTemplate<String, Object> redisTemplate;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	public User getUserById(Long userId, long ttl) {
		String key = USER_CACHE_PREFIX + userId;

		Object cachedValue = redisTemplate.opsForValue().get(key);

		if (cachedValue != null) {
			UserVo userVo = objectMapper.convertValue(cachedValue, UserVo.class);
			return UserVo.toUser(userVo);
		}

		return fetchAndCacheUser(userId, key, ttl);
	}

	public void deleteUserById(Long userId) {
		String key = USER_CACHE_PREFIX + userId;

		redisTemplate.delete(key);
	}

	private User fetchAndCacheUser(Long userId, String key, long ttl) {
		User user = userRepository.findUserById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

		UserVo userVo = UserVo.from(user);

		redisTemplate.opsForValue().set(key, userVo, ttl, TimeUnit.SECONDS);

		return user;
	}

}
