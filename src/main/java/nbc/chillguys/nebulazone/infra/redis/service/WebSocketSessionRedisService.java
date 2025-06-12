package nbc.chillguys.nebulazone.infra.redis.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketSessionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final String SESSION_USER_PREFIX = "session:user:";
	private static final String SESSION_ROOM_PREFIX = "session:room:";
	private static final Duration SESSION_TTL = Duration.ofHours(12);

	// 세션ID : user 매핑
	public void registerUser(String sessionId, SessionUser sessionUser) {
		try {
			String key = SESSION_USER_PREFIX + sessionId;
			redisTemplate.opsForValue().set(key, sessionUser, SESSION_TTL);
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

			redisTemplate.delete(userKey);
			redisTemplate.delete(roomKey);
		} catch (Exception e) {
			log.error("세션 삭제 실패");
		}
	}

}
