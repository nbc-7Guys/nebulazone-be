package nbc.chillguys.nebulazone.application.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponses;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/rooms")
	public ResponseEntity<CreateChatRoomResponse> createChatRoom(
		@AuthenticationPrincipal User user,
		@RequestBody CreateChatRoomRequest request
	) {
		CreateChatRoomResponse chatRoom = chatService.getOrCreate(user, request);
		return ResponseEntity.ok(chatRoom);
	}

	@GetMapping("/rooms")
	public ResponseEntity<FindChatRoomResponses> findChatRoom(
		@AuthenticationPrincipal User user
	) {
		FindChatRoomResponses chatRooms = chatService.findChatRooms(user);
		return ResponseEntity.ok(chatRooms);
	}

	@GetMapping("/rooms/history/{roomId}")
	public ResponseEntity<List<FindChatHistoryResponse>> findChatHistories(
		@AuthenticationPrincipal User user,
		@PathVariable("roomId") Long roomId
	) {
		List<FindChatHistoryResponse> chatHistories = chatService.findChatHistories(user, roomId);
		return ResponseEntity.ok(chatHistories);
	}

	@DeleteMapping("/rooms/{roomId}")
	public ResponseEntity<String> leaveChatRoom(
		@AuthenticationPrincipal User user,
		@PathVariable("roomId") Long roomId
	) {
		chatService.exitChatRoom(user, roomId);
		return ResponseEntity.ok("성공적으로 채팅방을 나갔습니다.");
	}

}
