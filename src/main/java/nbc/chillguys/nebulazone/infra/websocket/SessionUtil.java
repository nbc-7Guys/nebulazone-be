package nbc.chillguys.nebulazone.infra.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

public class SessionUtil {

	private static final Map<String, AuthUser> sessionIdToUserId = new ConcurrentHashMap<>();
	private static final Map<String, Long> sessionIdToRoomId = new ConcurrentHashMap<>();

	// 세션ID : userId 매핑
	public static void registerUser(String sessionId, AuthUser authUser) {
		sessionIdToUserId.put(sessionId, authUser);
	}

	// 세션ID : roomId 매핑
	public static void registerRoom(String sessionId, long roomId) {
		sessionIdToRoomId.put(sessionId, roomId);
	}

	// 세션ID로 userId 찾기
	public static AuthUser getUserIdBySessionId(String sessionId) {
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
