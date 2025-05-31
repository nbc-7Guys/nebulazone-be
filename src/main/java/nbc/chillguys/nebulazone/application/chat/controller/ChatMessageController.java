package nbc.chillguys.nebulazone.application.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendMessageCommand;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatService chatService;

	@MessageMapping("/send/{roomId}")
	public void sendMessage(
		@DestinationVariable Long roomId,
		@Payload ChatSendMessageCommand command,
		@AuthenticationPrincipal AuthUser authUser
	) {
		chatService.sendMessage(authUser, roomId, command);
	}

}
