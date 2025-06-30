package nbc.chillguys.nebulazone.infra.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.infra.websocket.interceptor.AuthenticationChannelInterceptor;

/**
 * WebSocket 통신을 위한 설정 클래스
 *
 * <p>STOMP 프로토콜을 사용하는 WebSocket 메시지 브로커 설정을 담당하며,
 * 실시간 채팅을 위한 엔드포인트와 메시지 라우팅을 구성</p>
 *
 * @author 박형우
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final AuthenticationChannelInterceptor authenticationChannelInterceptor;

	/**
	 * 메시지 브로커 경로(prefix) 설정
	 *
	 * <p>STOMP 프로토콜의 메시지 라우팅을 위한 prefix를 설정합니다:</p>
	 * <ul>
	 *   <li>클라이언트 → 서버 메시지 전송: "/chat" prefix 사용</li>
	 *   <li>서버 → 클라이언트 메시지 전달(브로드캐스트): "/topic" prefix 구독</li>
	 * </ul>
	 *
	 * @param registry 메시지 브로커 레지스트리
	 * @author 박형우
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/chat");
		registry.enableSimpleBroker("/topic");
	}

	/**
	 * STOMP 엔드포인트 등록
	 *
	 * <p>WebSocket 연결을 위한 엔드포인트를 설정합니다:</p>
	 * <ul>
	 *   <li>클라이언트가 WebSocket 연결을 시도할 경로: "/ws"</li>
	 *   <li>CORS 허용을 위해 모든 Origin 패턴 허용</li>
	 *   <li>WebSocket 미지원 환경을 위한 SockJS fallback 적용</li>
	 * </ul>
	 *
	 * @param registry STOMP 엔드포인트 레지스트리
	 * @author 박형우
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
			.setAllowedOriginPatterns("*")
			.withSockJS();
	}

	/**
	 * 클라이언트 인바운드 채널 인터셉터 설정
	 *
	 * <p>클라이언트로부터 수신되는 STOMP 메시지에 대한 인터셉터를 등록합니다.
	 * 인증/인가 등의 전처리를 담당합니다.</p>
	 *
	 * @param registration 채널 등록 설정
	 * @author 박형우
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(authenticationChannelInterceptor);
	}

}
