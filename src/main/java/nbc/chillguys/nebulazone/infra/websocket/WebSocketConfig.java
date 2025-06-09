package nbc.chillguys.nebulazone.infra.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final AuthenticationChannelInterceptor authenticationChannelInterceptor;

	/**
	 * 메시지 브로커 경로(prefix) 설정 메서드 <br/>
	 * - 클라이언트 → 서버 메시지 전송: "/chat" prefix 사용 <br/>
	 * - 서버 → 클라이언트 메시지 전달(브로드캐스트): "/topic" prefix 구독
	 *
	 * @author 박형우
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/chat");
		registry.enableSimpleBroker("/topic");
	}

	/**
	 * STOMP 엔드포인트 등록 <br/>
	 * - 클라이언트가 WebSocket 연결을 시도할 경로("/ws")를 등록 <br/>
	 * - CORS 허용을 위해 모든 Origin을 허용 <br/>
	 * - WebSocket 미지원 환경을 위한 SockJS fallback을 적용 <br/>
	 *
	 * @author 박형우
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.withSockJS();
	}

	/**
	 * 클라이언트 인바운드 채널(STOMP) 인터셉터 설정 메서드 <br/>
	 * - 인증/인가 등 메시지 수신 시 필요한 인터셉터를 등록
	 *
	 * @author 박형우
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(authenticationChannelInterceptor);
	}

}
