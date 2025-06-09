package nbc.chillguys.nebulazone.infra.websocket;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.infra.security.JwtUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomUserRepository chatRoomUserRepository;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		// STOMP CONNECT
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {

			// 헤더에서 토큰 꺼내기
			String token = accessor.getFirstNativeHeader("Authorization");

			if (token == null) {
				throw new IllegalArgumentException("No token provided");
			}

			// "Bearer " 제거
			if (token.startsWith("Bearer ")) {
				token = token.substring("Bearer ".length());
			}

			try {
				// jWT 토큰 파싱 및 검증
				AuthUser authUserFromToken = jwtUtil.getAuthUserFromToken(token);

				// accessor.setUser에는 Principal 타입이 필요하기 때문에 UsernamePasswordAuthenticationToken으로 감싸기
				Principal principal = new UsernamePasswordAuthenticationToken(String.valueOf(authUserFromToken.getId()),
					null, authUserFromToken.getAuthorities());

				accessor.setUser(principal);

				// CONNECT 단계 에서는 유저 정보만 매핑
				SessionUtil.registerUser(accessor.getSessionId(), authUserFromToken);

			} catch (Exception e) {
				log.warn("JWT 파싱 또는 Principal 세팅 예외: {}", e);
				throw e;
			}

		}

		// STOMP SUBSCRIBE
		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

			// 구독할 경로 추출
			String destination = accessor.getDestination();

			if (destination != null && destination.startsWith("/topic/chat/")) {
				String roomIdStr = destination.substring("/topic/chat/".length());
				try {
					Long roomId = Long.valueOf(roomIdStr);
					AuthUser authUser = SessionUtil.getUserIdBySessionId(accessor.getSessionId());

					// 채팅방 존재 여부 확인
					boolean existsChatRoomById = chatRoomRepository.existsChatRoomById(roomId);
					if (!existsChatRoomById) {
						throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
					}

					// 채팅방에 참여중인 유저인지 확인
					boolean isParticipant = chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(roomId,
						authUser.getId());
					if (!isParticipant) {
						throw new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
					}

					SessionUtil.registerRoom(accessor.getSessionId(), roomId);
				} catch (NumberFormatException e) {
					log.warn("방번호 추출 실패: {}", roomIdStr);
				}

			}
		}

		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	}
}
