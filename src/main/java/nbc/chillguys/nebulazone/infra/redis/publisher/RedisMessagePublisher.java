package nbc.chillguys.nebulazone.infra.redis.publisher;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.infra.redis.dto.AuctionPubSubMessage;
import nbc.chillguys.nebulazone.infra.redis.dto.ChatPubSubMessage;

/**
 * Redis Pub/Sub을 통해 채팅 메시지를 발행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {

	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 채팅 메시지를 Redis 채널로 발행
	 *
	 * @param roomId 채팅방 ID
	 * @param chatMessageInfo 발행할 채팅 메시지 정보
	 */
	@Async
	public void publishChatMessage(Long roomId, ChatMessageInfo chatMessageInfo) {
		try {
			// ChatMessageInfo를 ChatPubSubMessage로 변환
			ChatPubSubMessage pubSubMessage = ChatPubSubMessage.from(roomId, chatMessageInfo);

			// 채널명 생성 (chat:room:{roomId})
			String channelName = ChatPubSubMessage.getChannelName(roomId);

			// Redis 채널로 메시지 발행
			redisTemplate.convertAndSend(channelName, pubSubMessage);
		} catch (Exception e) {
			log.error("Redis 메시지 발행 중 오류 발생 - roomId: {}, error: {}", roomId, e.getMessage(), e);
		}
	}

	public void publishAuctionUpdate(Long auctionId, String updateType, Object data) {
		try {
			AuctionPubSubMessage message = AuctionPubSubMessage.of(auctionId, updateType, data);
			String channelName = AuctionPubSubMessage.getChannelName(auctionId, updateType);
			redisTemplate.convertAndSend(channelName, message);

			log.debug("경매 메시지 발행 완료 - channel: {}", channelName);
		} catch (Exception e) {
			log.error("경매 Redis 메시지 발행 실패 - auctionId: {}, type: {}, error: {}",
				auctionId, updateType, e.getMessage(), e);
		}
	}

}
