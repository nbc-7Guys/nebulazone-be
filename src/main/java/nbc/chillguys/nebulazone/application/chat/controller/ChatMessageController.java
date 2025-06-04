package nbc.chillguys.nebulazone.application.chat.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;
import nbc.chillguys.nebulazone.config.websocket.AuthenticationChannelInterceptor;
import nbc.chillguys.nebulazone.config.websocket.SessionUtil;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/send/{roomId}")
	public void sendMessage(
		@DestinationVariable Long roomId,
		@Payload ChatSendMessageCommand command,
		StompHeaderAccessor accessor
	) {
		String sessionId = accessor.getSessionId();
		// Principal principal = AuthenticationChannelInterceptor.sessionPrincipalMap.get(sessionId);

		Long userId = SessionUtil.getUserIdBySessionId(sessionId);

		chatMessageService.sendMessage(userId, roomId, command);

	}

}
