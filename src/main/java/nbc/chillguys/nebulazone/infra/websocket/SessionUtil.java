package nbc.chillguys.nebulazone.infra.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nbc.chillguys.nebulazone.domain.user.entity.User;

public class SessionUtil {

	private static final Map<String, User> sessionIdToUser = new ConcurrentHashMap<>();
	private static final Map<String, Long> sessionIdToRoomId = new ConcurrentHashMap<>();

	// 세션ID : User 매핑
	public static void registerUser(String sessionId, User user) {
		sessionIdToUser.put(sessionId, user);
	}

	// 세션ID : roomId 매핑
	public static void registerRoom(String sessionId, long roomId) {
		sessionIdToRoomId.put(sessionId, roomId);
	}

	// 세션ID로 User 찾기
	public static User getUserIdBySessionId(String sessionId) {
		return sessionIdToUser.get(sessionId);
	}

	// 세션ID로 roomId찾기
	public static Long getRoomIdBySessionId(String sessionId) {
		return sessionIdToRoomId.get(sessionId);
	}

	// 세션 종료 시 삭제
	public static void unregisterSession(String sessionId) {
		sessionIdToUser.remove(sessionId);
		sessionIdToRoomId.remove(sessionId);
	}

}
