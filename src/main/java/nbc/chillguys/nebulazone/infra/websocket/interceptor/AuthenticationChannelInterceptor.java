package nbc.chillguys.nebulazone.infra.websocket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.infra.redis.service.WebSocketSessionRedisService;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;
	private final WebSocketSessionRedisService webSocketSessionRedisService;

	/**
	 * WebSocket STOMP 인바운드 메시지 인증 및 권한 검증 인터셉터 <br>
	 * <p>
	 * - CONNECT: JWT 토큰을 검증하고, Principal 및 세션-유저 매핑을 수행<br>
	 * - SUBSCRIBE: 채팅방/알림 구독 권한을 검증하고, 세션 매핑을 수행
	 * </p>
	 *
	 * @author 박형우
	 */
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// CONNECT: 최초 연결 시 JWT 인증 처리
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {

			String token = accessor.getFirstNativeHeader("Authorization");

			if (token == null) {
				throw new IllegalArgumentException("No token provided");
			}

			if (token.startsWith("Bearer ")) {
				token = token.substring("Bearer ".length());
			}

			try {
				AuthUser authUserFromToken = jwtUtil.getAuthUserFromToken(token);
				SessionUser user = SessionUser.from(authUserFromToken);

				// 세션과 유저 매핑 (메모리 or Redis 등)
				webSocketSessionRedisService.registerUser(accessor.getSessionId(), user);

			} catch (Exception e) {
				log.warn("JWT 파싱 또는 Principal 세팅 예외: {}", e);
				throw e;
			}

		}

		// SUBSCRIBE: 구독 시 권한 체크
		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

			String destination = accessor.getDestination();
			SessionUser user = webSocketSessionRedisService.getUserIdBySessionId(accessor.getSessionId());

			if (user == null) {
				throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
			}

			// 채팅방 구독 권한 체크
			if (destination != null && destination.startsWith("/topic/chat/")) {
				String roomIdStr = destination.substring("/topic/chat/".length());
				try {
					Long roomId = Long.valueOf(roomIdStr);

					// 채팅방 존재 확인
					boolean existsChatRoomById = chatRoomRepository.existsChatRoomById(roomId);
					if (!existsChatRoomById) {
						throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
					}

					// 채팅방 참가자 여부 확인
					boolean isParticipant = chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId,
						user.id());
					if (!isParticipant) {
						throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
					}

					// 세션과 채팅방 매핑
					webSocketSessionRedisService.registerRoom(accessor.getSessionId(), roomId);
				} catch (NumberFormatException e) {
					log.warn("방번호 추출 실패: {}", roomIdStr);
				}
			}
			// 알림 구독 권한 체크
			else if (destination != null && destination.startsWith("/topic/notification/")) {
				String userIdStr = destination.substring("/topic/notification/".length());
				try {
					Long targetUserId = Long.valueOf(userIdStr);

					// 자신의 알림만 구독 가능하도록 권한 체크
					if (!user.id().equals(targetUserId)) {
						log.warn("알림 구독 권한 없음 - sessionUserId: {}, targetUserId: {}", user.id(), targetUserId);
						throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
					}

				} catch (NumberFormatException e) {
					log.warn("알림 구독 대상 사용자 ID 추출 실패: {}", userIdStr);
					throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
				}
			}
		}

		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	}
}
