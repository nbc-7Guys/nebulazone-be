package nbc.chillguys.nebulazone.application.chat.controller;

import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/send/{roomId}")
	public void sendMessage(
		@DestinationVariable Long roomId,
		@Payload ChatSendTextMessageCommand command,
		StompHeaderAccessor accessor
	) {
		String sessionId = accessor.getSessionId();
		chatMessageService.sendTextMessage(sessionId, roomId, command);
	}

	@PostMapping(value = "/send/image/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void sendImage(
		@AuthenticationPrincipal User User,
		@PathVariable Long roomId,
		@RequestPart("image") MultipartFile multipartFile,
		@RequestPart("meta") String type
	) {
		chatMessageService.sendImageMessage(User, multipartFile, roomId, type);
	}

}
