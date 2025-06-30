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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

	/**
	 * 채팅방 생성 또는 기존 채팅방 조회
	 *
	 * <p>특정 상품에 대해 구매자와 판매자 간의 채팅방을 생성하거나 기존 채팅방 정보를 반환</p>
	 *
	 * @param user 인증된 사용자 정보
	 * @param request 채팅방 생성 요청 데이터 (상품 ID 포함)
	 * @return 생성되거나 조회된 채팅방 정보
	 * @author 박형우
	 */
	@PostMapping("/rooms")
	public ResponseEntity<CreateChatRoomResponse> createChatRoom(
		@AuthenticationPrincipal User user,
		@RequestBody @Valid CreateChatRoomRequest request
	) {
		CreateChatRoomResponse chatRoom = chatService.getOrCreate(user, request);
		return ResponseEntity.ok(chatRoom);
	}

	/**
	 * 사용자가 참여 중인 모든 채팅방 조회
	 *
	 * <p>현재 로그인한 사용자가 참여하고 있는 모든 채팅방 목록을 반환</p>
	 *
	 * @param user 인증된 사용자 정보
	 * @return 사용자 참여 채팅방 목록
	 * @author 박형우
	 */
	@GetMapping("/rooms")
	public ResponseEntity<FindChatRoomResponses> findChatRoom(
		@AuthenticationPrincipal User user
	) {
		FindChatRoomResponses chatRooms = chatService.findChatRooms(user);
		return ResponseEntity.ok(chatRooms);
	}

	/**
	 * 특정 채팅방의 채팅 기록 조회
	 *
	 * <p>지정된 채팅방의 모든 채팅 메시지 기록을 시간순으로 반환</p>
	 *
	 * @param user 인증된 사용자 정보
	 * @param roomId 조회할 채팅방 ID
	 * @return 채팅방의 메시지 기록 목록
	 * @author 박형우
	 */
	@GetMapping("/rooms/history/{roomId}")
	public ResponseEntity<List<FindChatHistoryResponse>> findChatHistories(
		@AuthenticationPrincipal User user,
		@PathVariable("roomId") @NotBlank(message = "roomId를 입력해주세요") Long roomId,
		@RequestParam(required = false) Long lastId,
		@RequestParam(defaultValue = "30") int size
	) {
		List<FindChatHistoryResponse> chatHistories = chatService.findChatHistories(user, roomId, lastId, size);
		return ResponseEntity.ok(chatHistories);
	}

	/**
	 * 채팅방 나가기
	 *
	 * <p>사용자가 특정 채팅방에서 나가며, 다른 참여자에게 나감 알림을 전송</p>
	 *
	 * @param user 인증된 사용자 정보
	 * @param roomId 나갈 채팅방 ID
	 * @return 성공 메시지
	 * @author 박형우
	 */
	@DeleteMapping("/rooms/{roomId}")
	public ResponseEntity<String> leaveChatRoom(
		@AuthenticationPrincipal User user,
		@PathVariable("roomId") @NotBlank(message = "roomId를 입력해주세요") Long roomId
	) {
		chatService.exitChatRoom(user, roomId);
		return ResponseEntity.ok("성공적으로 채팅방을 나갔습니다.");
	}

}
