package nbc.chillguys.nebulazone.infra.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

public class SessionUtil {

	private static final Map<String, AuthUser> sessionIdToAuthUser = new ConcurrentHashMap<>();
	private static final Map<String, Long> sessionIdToRoomId = new ConcurrentHashMap<>();

	// 세션ID : AuthUser 매핑
	public static void registerUser(String sessionId, AuthUser authUser) {
		sessionIdToAuthUser.put(sessionId, authUser);
	}

	// 세션ID : roomId 매핑
	public static void registerRoom(String sessionId, long roomId) {
		sessionIdToRoomId.put(sessionId, roomId);
	}

	// 세션ID로 AuthUser 찾기
	public static AuthUser getUserIdBySessionId(String sessionId) {
		return sessionIdToAuthUser.get(sessionId);
	}

	// 세션ID로 roomId찾기
	public static Long getRoomIdBySessionId(String sessionId) {
		return sessionIdToRoomId.get(sessionId);
	}

	// 세션 종료 시 삭제
	public static void unregisterSession(String sessionId) {
		sessionIdToAuthUser.remove(sessionId);
		sessionIdToRoomId.remove(sessionId);
	}

}
