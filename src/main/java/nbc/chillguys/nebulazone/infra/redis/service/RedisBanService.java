package nbc.chillguys.nebulazone.infra.redis.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisBanService {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String BAN_PREFIX = "ban:";

	public void registerBan(String ipAddress, long seconds) {
		redisTemplate.opsForValue().set(BAN_PREFIX + ipAddress, true, Duration.ofSeconds(seconds));
	}

	public boolean isBanned(String ipAddress) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BAN_PREFIX + ipAddress));
	}

	public void unban(String ipAddress) {
		redisTemplate.delete(BAN_PREFIX + ipAddress);
	}
}
