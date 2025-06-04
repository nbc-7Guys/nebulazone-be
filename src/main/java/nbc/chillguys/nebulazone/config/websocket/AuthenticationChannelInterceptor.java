package nbc.chillguys.nebulazone.config.websocket;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

	public static final Map<String, Principal> sessionPrincipalMap = new ConcurrentHashMap<>();

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		// STOMP의 타입이 CONNECT 인지 확인
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 헤더에서 토큰 꺼내기
			String token = accessor.getFirstNativeHeader("Authorization");
			log.info("CONNECT 프레임 Authorization 헤더: {}", token);
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
				Principal authentication = new UsernamePasswordAuthenticationToken(authUserFromToken, null,
					authUserFromToken.getAuthorities());
				// accessor.setUser(authentication);
				sessionPrincipalMap.put(accessor.getSessionId(), authentication);
				log.info("세팅 완료: {}",  authentication.getName());
			} catch (Exception e) {
				System.out.println("JWT 파싱 또는 Principal 세팅 예외: " + e.getMessage());
				throw e;
			}
			log.info("preSend: 세션 ID = {}, principal = {}",
				accessor.getSessionId(),
				accessor.getUser() == null ? "null" : accessor.getUser().getName()
			);
		}
		// return ChannelInterceptor.super.preSend(message, channel);
		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
		// return message;
	}
}
