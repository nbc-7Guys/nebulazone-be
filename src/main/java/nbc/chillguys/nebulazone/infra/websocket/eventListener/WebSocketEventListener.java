package nbc.chillguys.nebulazone.infra.websocket.eventListener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.infra.websocket.SessionUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final ChatMessageService chatMessageService;

	@EventListener
	public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = accessor.getSessionId();

		Long roomId = SessionUtil.getRoomIdBySessionId(sessionId);

		if (roomId != null) {
			// redis -> DB
			chatMessageService.saveMessagesToDb(roomId);
		}

		SessionUtil.unregisterSession(sessionId);

	}

}
