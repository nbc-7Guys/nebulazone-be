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
import nbc.chillguys.nebulazone.infra.redis.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserCacheService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	private static final String USER_CACHE_PREFIX = "user:";

	public User getUserById(Long userId, long ttl) {
		String key = USER_CACHE_PREFIX + userId;

		Object cachedValue = redisTemplate.opsForValue().get(key);

		if (cachedValue != null) {
			UserDto userDto = objectMapper.convertValue(cachedValue, UserDto.class);
			return UserDto.toUser(userDto);
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

		UserDto userDto = UserDto.from(user);

		redisTemplate.opsForValue().set(key, userDto, ttl, TimeUnit.SECONDS);

		return user;
	}

}
