package nbc.chillguys.nebulazone.config.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final ChatMessageService chatMessageService;

	@EventListener
	public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		Long userId = SessionUtil.getUserIdBySessionId(sessionId);
		Long roomId = SessionUtil.getRoomIdBySessionId(sessionId);

		log.info("연결 해제 세션 : {}, 유저 : {}, 방 : {}", sessionId, userId, roomId);

		if (roomId != null) {
			// redis -> DB
			chatMessageService.saveMessagesToDb(roomId);
			log.info("saveMessagesToDb 실행 완");
		}

		SessionUtil.unregisterSession(sessionId);

	}

}
