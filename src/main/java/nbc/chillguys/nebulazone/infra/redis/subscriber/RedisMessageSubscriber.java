package nbc.chillguys.nebulazone.infra.redis.subscriber;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.infra.redis.dto.AuctionPubSubMessage;
import nbc.chillguys.nebulazone.infra.redis.dto.ChatPubSubMessage;

/**
 * Redis Pub/Sub 메시지를 구독하여 WebSocket으로 브로드캐스트하는 서비스
 *
 * <p>Redis에서 발행된 채팅 메시지를 수신하여 WebSocket을 통해
 * 해당 채팅방의 모든 구독자들에게 실시간으로 전달</p>
 *
 * @author 박형우
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
	 * <p>Redis Pub/Sub 채널에서 수신한 채팅 메시지를 파싱하고,
	 * 해당 채팅방을 구독하고 있는 모든 클라이언트에게 WebSocket을 통해 전달</p>
	 *
	 * @param message Redis에서 수신한 메시지
	 * @param pattern 구독한 채널 패턴 (사용되지 않음)
	 * @author 박형우
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			// Redis 메시지를 문자열로 변환
			String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
			String channel = new String(message.getChannel());

			if (channel.startsWith("chat:room")) {
				// JSON을 ChatPubSubMessage 객체로 역직렬화
				ChatPubSubMessage pubSubMessage = objectMapper.readValue(messageBody, ChatPubSubMessage.class);

				// ChatPubSubMessage를 ChatMessageInfo로 변환
				ChatMessageInfo chatMessageInfo = pubSubMessage.toChatMessageInfo();

				// WebSocket 구독자들에게 브로드캐스트
				String destination = "/topic/chat/" + pubSubMessage.getRoomId();
				messagingTemplate.convertAndSend(destination, chatMessageInfo);

			} else if (channel.startsWith("auction:")) {

				AuctionPubSubMessage auctionMessage = objectMapper.readValue(messageBody,
					AuctionPubSubMessage.class);

				Long auctionId = auctionMessage.auctionId();
				String updateType = auctionMessage.updateType();

				String destination = "/topic/auction/" + auctionId + "/" + updateType;

				messagingTemplate.convertAndSend(destination, auctionMessage.data());

			}

		} catch (Exception e) {
			log.error("Redis 메시지 처리 중 오류 발생 - 채널: {}, error: {}", message.getChannel(), e.getMessage(), e);
		}
	}
}
