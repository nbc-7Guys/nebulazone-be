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
import nbc.chillguys.nebulazone.infra.security.jwt.JwtUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;

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
				SessionUtil.registerUser(accessor.getSessionId(), authUserFromToken.getId());

			} catch (Exception e) {
				log.warn("JWT 파싱 또는 Principal 세팅 예외: {}", e.getMessage());
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
					SessionUtil.registerRoom(accessor.getSessionId(), roomId);
				} catch (NumberFormatException e) {
					log.warn("방번호 추출 실패: {}", roomIdStr);
				}

			}
		}

		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
	}
}
