package nbc.chillguys.nebulazone.infra.redis.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketSessionRedisService {

	private static final String SESSION_USER_PREFIX = "session:user:";
	private static final String USER_SESSION_PREFIX = "user:session:";
	private static final String SESSION_ROOM_PREFIX = "session:room:";
	private static final String ONLINE_USERS_KEY = "online:users";
	private static final Duration SESSION_TTL = Duration.ofHours(12);
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	// 세션ID : user 매핑
	public void registerUser(String sessionId, SessionUser sessionUser) {
		try {
			String key = SESSION_USER_PREFIX + sessionId;
			redisTemplate.opsForValue().set(key, sessionUser, SESSION_TTL);

			registerOnlineUser(sessionUser.id(), sessionId);

		} catch (Exception e) {
			log.error("세션 유저 등록 실패");
		}
	}

	// 세션ID : roomId 매핑
	public void registerRoom(String sessionId, long roomId) {
		try {
			String key = SESSION_ROOM_PREFIX + sessionId;
			redisTemplate.opsForValue().set(key, roomId, SESSION_TTL);
		} catch (Exception e) {
			log.error("세션 룸 등록 실패");
		}
	}

	// 세션ID로 SessionUser 찾기
	public SessionUser getUserIdBySessionId(String sessionId) {
		try {
			String key = SESSION_USER_PREFIX + sessionId;
			Object result = redisTemplate.opsForValue().get(key);

			if (result == null) {
				return null;
			}

			return objectMapper.convertValue(result, SessionUser.class);
		} catch (Exception e) {
			log.error("세션ID 유저 찾기 실패 : {}", e.getMessage(), e);
			return null;
		}
	}

	// 세션ID로 roomId찾기
	public Long getRoomIdBySessionId(String sessionId) {
		try {
			String key = SESSION_ROOM_PREFIX + sessionId;
			Object result = redisTemplate.opsForValue().get(key);
			if (result == null) {
				return null;
			}
			return Long.parseLong(result.toString());
		} catch (Exception e) {
			log.error("세션ID roomId 찾기 실패");
			return null;
		}
	}

	// 세션 종료 시 삭제
	public void unregisterSession(String sessionId) {
		try {
			String userKey = SESSION_USER_PREFIX + sessionId;
			String roomKey = SESSION_ROOM_PREFIX + sessionId;

			SessionUser sessionUser = getUserIdBySessionId(sessionId);
			if (sessionUser != null) {
				unregisterOnlineUser(sessionUser.id());
			}

			redisTemplate.delete(userKey);
			redisTemplate.delete(roomKey);

		} catch (Exception e) {
			log.error("세션 삭제 실패");
		}
	}

	public void registerOnlineUser(Long userId, String sessionId) {
		try {
			redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);

			String userSessionKey = USER_SESSION_PREFIX + userId;
			redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TTL);

		} catch (Exception e) {
			log.error("온라인 유저 등록 실패 - userId: {}, error: {}", userId, e.getMessage());
		}
	}

	public void unregisterOnlineUser(Long userId) {
		try {
			redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);

			String userSessionKey = USER_SESSION_PREFIX + userId;
			redisTemplate.delete(userSessionKey);

		} catch (Exception e) {
			log.error("온라인 유저 삭제 실패 - userId: {}, error: {}", userId, e.getMessage());
		}
	}

	public boolean isOnlineUser(Long userId) {
		try {
			Boolean isUserOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId);
			return Boolean.TRUE.equals(isUserOnline);
		} catch (Exception e) {
			log.error("온라인 상태 확인 실패 - userId: {}, error: {}", userId, e.getMessage());
			return false;
		}
	}

	public String getSessionIdByUserId(Long userId) {
		try {
			String userSessionKey = USER_SESSION_PREFIX + userId;
			Object result = redisTemplate.opsForValue().get(userSessionKey);
			if (result == null) {
				return null;
			}
			return result.toString();
		} catch (Exception e) {
			log.error("사용자 세션 ID 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
			return null;
		}
	}

}
