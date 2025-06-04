package nbc.chillguys.nebulazone.infra.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionUtil {

	private static final Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();
	private static final Map<String, Long> sessionIdToRoomId = new ConcurrentHashMap<>();

	// 세션ID : userId 매핑
	public static void registerUser(String sessionId, long userId) {
		sessionIdToUserId.put(sessionId, userId);
	}

	// 세션ID : roomId 매핑
	public static void registerRoom(String sessionId, long roomId) {
		sessionIdToRoomId.put(sessionId, roomId);
	}

	// 세션ID로 userId 찾기
	public static Long getUserIdBySessionId(String sessionId) {
		return sessionIdToUserId.get(sessionId);
	}

	// 세션ID로 roomId찾기
	public static Long getRoomIdBySessionId(String sessionId) {
		return sessionIdToRoomId.get(sessionId);
	}

	// 세션 종료 시 삭제
	public static void unregisterSession(String sessionId) {
		sessionIdToUserId.remove(sessionId);
		sessionIdToRoomId.remove(sessionId);
	}

}
