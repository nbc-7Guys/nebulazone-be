package nbc.chillguys.nebulazone.infra.redis.subscriber;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.infra.redis.dto.ChatPubSubMessage;

/**
 * Redis Pub/Sub 메시지를 구독하여 WebSocket으로 브로드캐스트하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

	private final SimpMessagingTemplate messagingTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * Redis에서 발행된 메시지를 수신하여 WebSocket으로 브로드캐스트
	 *
	 * @param message Redis에서 수신한 메시지
	 * @param pattern 구독한 채널 패턴
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			// Redis 메시지를 문자열로 변환
			String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

			// JSON을 ChatPubSubMessage 객체로 역직렬화
			ChatPubSubMessage pubSubMessage = objectMapper.readValue(messageBody, ChatPubSubMessage.class);

			// ChatPubSubMessage를 ChatMessageInfo로 변환
			ChatMessageInfo chatMessageInfo = pubSubMessage.toChatMessageInfo();

			// WebSocket 구독자들에게 브로드캐스트
			String destination = "/topic/chat/" + pubSubMessage.getRoomId();
			messagingTemplate.convertAndSend(destination, chatMessageInfo);

		} catch (Exception e) {
			log.error("Redis 메시지 처리 중 오류 발생 - 채널: {}, error: {}", message.getChannel(), e.getMessage(), e);
		}
	}
}
