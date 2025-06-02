package nbc.chillguys.nebulazone.config.websocket;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.infra.security.jwt.JwtUtil;

@Component
@RequiredArgsConstructor
public class AuthenticationChannelInterceptor implements ChannelInterceptor {

	private final JwtUtil jwtUtil;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		// STOMP의 타입이 CONNECT 인지 확인
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// 헤더에서 토큰 꺼내기
			String token = accessor.getFirstNativeHeader("Authorization");
			if (token == null) {
				throw new IllegalArgumentException("No token provided");
			}
			// jWT 토큰 파싱 및 검증
			AuthUser authUserFromToken = jwtUtil.getAuthUserFromToken(token);
			// accessor.setUser에는 Principal 타입이 필요하기 때문에 UsernamePasswordAuthenticationToken으로 감싸기
			Principal principal = new UsernamePasswordAuthenticationToken(
				authUserFromToken, null, authUserFromToken.getAuthorities()
			);
			accessor.setUser(principal);
		}

		return ChannelInterceptor.super.preSend(message, channel);
	}
}
