package nbc.chillguys.nebulazone.application.chat.controller;

import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.dto.request.ImageMessageRequest;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

/**
 * 채팅 메시지 전송을 담당하는 WebSocket 및 REST 컨트롤러
 *
 * <p>텍스트 메시지는 WebSocket(STOMP)을 통해, 이미지 메시지는 HTTP를 통해 전송.</p>
 *
 * @author 박형우
 */
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	/**
	 * WebSocket을 통한 텍스트 메시지 전송
	 *
	 * <p>STOMP 프로토콜을 사용하여 실시간으로 텍스트 메시지를 전송</p>
	 *
	 * @param roomId 메시지를 전송할 채팅방 ID
	 * @param command 전송할 텍스트 메시지 명령 객체
	 * @param accessor STOMP 헤더 접근자 (세션 ID 추출용)
	 * @author 박형우
	 */
	@MessageMapping("/send/{roomId}")
	public void sendMessage(
		@DestinationVariable("roomId") Long roomId,
		@Valid @Payload ChatSendTextMessageCommand command,
		StompHeaderAccessor accessor
	) {
		String sessionId = accessor.getSessionId();
		chatMessageService.sendTextMessage(sessionId, roomId, command);
	}

	/**
	 * HTTP를 통한 이미지 메시지 전송
	 *
	 * <p>Multipart 형태로 이미지 파일을 업로드하고 채팅방에 이미지 메시지를 전송</p>
	 *
	 * @param user 인증된 사용자 정보
	 * @param roomId 메시지를 전송할 채팅방 ID
	 * @author 박형우
	 */
	@PostMapping(value = "/send/image/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public void sendImage(
		@AuthenticationPrincipal User user,
		@PathVariable("roomId") Long roomId,
		@Valid @ModelAttribute ImageMessageRequest request
	) {
		chatMessageService.sendImageMessage(user, roomId, request);
	}

}
