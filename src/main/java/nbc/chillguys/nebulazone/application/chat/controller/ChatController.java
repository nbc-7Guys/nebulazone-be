package nbc.chillguys.nebulazone.application.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/rooms")
	public ResponseEntity<CreateChatRoomResponse> createChatRoom(@AuthenticationPrincipal AuthUser authUser,
		@RequestBody CreateChatRoomRequest request) {
		CreateChatRoomResponse chatRoom = chatService.createOrGet(authUser, request);
		return ResponseEntity.ok(chatRoom);
	}

}
