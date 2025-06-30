package nbc.chillguys.nebulazone.infra.redis.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

/**
 * WebSocket 세션 정보를 Redis에서 관리하는 서비스
 *
 * <p>WebSocket 연결된 사용자들의 세션 정보, 온라인 상태, 채팅방 참여 정보를
 * Redis에 저장하고 관리하여 다중 서버 환경에서의 세션 공유를 지원</p>
 *
 * @author 박형우
 */
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

	/**
	 * 사용자의 WebSocket 세션을 Redis에 등록
	 *
	 * <p>세션 ID와 사용자 정보를 매핑하고, 온라인 사용자 목록에 추가</p>
	 *
	 * @param sessionId WebSocket 세션 ID
	 * @param sessionUser 세션 사용자 정보
	 * @author 박형우
	 */
	public void registerUser(String sessionId, SessionUser sessionUser) {
		try {
			String key = SESSION_USER_PREFIX + sessionId;
			redisTemplate.opsForValue().set(key, sessionUser, SESSION_TTL);

			registerOnlineUser(sessionUser.id(), sessionId);

		} catch (Exception e) {
			log.error("세션 유저 등록 실패");
		}
	}

	/**
	 * 세션 ID와 채팅방 ID를 매핑하여 등록
	 *
	 * <p>특정 세션이 어느 채팅방에 참여하고 있는지 추적하기 위해 사용</p>
	 *
	 * @param sessionId WebSocket 세션 ID
	 * @param roomId 채팅방 ID
	 * @author 박형우
	 */
	public void registerRoom(String sessionId, long roomId) {
		try {
			String key = SESSION_ROOM_PREFIX + sessionId;
			redisTemplate.opsForValue().set(key, roomId, SESSION_TTL);
		} catch (Exception e) {
			log.error("세션 룸 등록 실패");
		}
	}

	/**
	 * 세션 ID로 사용자 정보 조회
	 *
	 * <p>WebSocket 세션 ID를 통해 해당 세션에 연결된 사용자 정보를 조회</p>
	 *
	 * @param sessionId WebSocket 세션 ID
	 * @return 세션에 연결된 사용자 정보, 없으면 null
	 * @author 박형우
	 */
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

	/**
	 * 세션 ID로 참여 중인 채팅방 ID 조회
	 *
	 * <p>특정 세션이 현재 참여하고 있는 채팅방의 ID를 반환</p>
	 *
	 * @param sessionId WebSocket 세션 ID
	 * @return 참여 중인 채팅방 ID, 없으면 null
	 * @author 박형우
	 */
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

	/**
	 * WebSocket 세션 종료 시 관련 정보 삭제
	 *
	 * <p>세션이 종료되면 해당 세션과 관련된 모든 Redis 데이터를 정리</p>
	 *
	 * @param sessionId 종료할 WebSocket 세션 ID
	 * @author 박형우
	 */
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

	/**
	 * 사용자를 온라인 목록에 등록
	 *
	 * <p>사용자가 온라인 상태임을 표시하고, 사용자 ID와 세션 ID를 매핑</p>
	 *
	 * @param userId 사용자 ID
	 * @param sessionId WebSocket 세션 ID
	 * @author 박형우
	 */
	public void registerOnlineUser(Long userId, String sessionId) {
		try {
			redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);

			String userSessionKey = USER_SESSION_PREFIX + userId;
			redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TTL);

		} catch (Exception e) {
			log.error("온라인 유저 등록 실패 - userId: {}, error: {}", userId, e.getMessage());
		}
	}

	/**
	 * 온라인 목록에서 사용자 제거
	 *
	 * <p>사용자가 오프라인 상태가 되면 온라인 목록에서 제거하고 관련 세션 정보를 삭제</p>
	 *
	 * @param userId 제거할 사용자 ID
	 * @author 박형우
	 */
	public void unregisterOnlineUser(Long userId) {
		try {
			redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);

			String userSessionKey = USER_SESSION_PREFIX + userId;
			redisTemplate.delete(userSessionKey);

		} catch (Exception e) {
			log.error("온라인 유저 삭제 실패 - userId: {}, error: {}", userId, e.getMessage());
		}
	}

	/**
	 * 사용자의 온라인 상태 확인
	 *
	 * <p>특정 사용자가 현재 온라인 상태인지 확인</p>
	 *
	 * @param userId 확인할 사용자 ID
	 * @return 온라인이면 true, 오프라인이면 false
	 * @author 박형우
	 */
	public boolean isOnlineUser(Long userId) {
		try {
			Boolean isUserOnline = redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId);
			return Boolean.TRUE.equals(isUserOnline);
		} catch (Exception e) {
			log.error("온라인 상태 확인 실패 - userId: {}, error: {}", userId, e.getMessage());
			return false;
		}
	}

	/**
	 * 사용자 ID로 현재 활성 세션 ID 조회
	 *
	 * <p>특정 사용자의 현재 활성화된 WebSocket 세션 ID를 반환</p>
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 활성 세션 ID, 없으면 null
	 * @author 박형우
	 */
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
