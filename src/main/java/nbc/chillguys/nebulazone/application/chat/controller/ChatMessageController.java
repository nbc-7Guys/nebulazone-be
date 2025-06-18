package nbc.chillguys.nebulazone.application.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;

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

	// @PostMapping(value = "/send/image/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// public void sendImage(
	// 	@AuthenticationPrincipal User User,
	// 	@PathVariable Long roomId,
	// 	@RequestPart("images") MultipartFile multipartFile,
	// 	@RequestPart("meta") String type
	// ) {
	// 	chatMessageService.sendImageMessage(User, multipartFile, roomId, type);
	// }

}
