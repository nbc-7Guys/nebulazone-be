package nbc.chillguys.nebulazone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.infra.websocket.AuthenticationChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final AuthenticationChannelInterceptor authenticationChannelInterceptor;

	// 메시지 브로커 어디로 보낼것인지, 받을 것인지
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 메시지를 보낼 prefix
		registry.setApplicationDestinationPrefixes("/chat");
		// 메시지를 구독할 prefix 브로커 = 중간에서 서버와 클라이언트를 연결해주는 역할
		registry.enableSimpleBroker("/topic");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws") // 클라이언트가 연결할 엔드포인트
			.setAllowedOriginPatterns("*") // CORS 설정
			.withSockJS(); // SockJS 설정
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(authenticationChannelInterceptor);
	}

}
