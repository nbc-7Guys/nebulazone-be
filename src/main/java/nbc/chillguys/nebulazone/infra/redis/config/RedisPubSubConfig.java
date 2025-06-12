package nbc.chillguys.nebulazone.infra.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.infra.redis.subscriber.RedisMessageSubscriber;

/**
 * Redis Pub/Sub 설정 클래스
 */
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

	private final RedisMessageSubscriber redisMessageSubscriber;

	/**
	 * 메시지 리스너 어댑터 설정
	 * Redis 메시지를 RedisMessageSubscriber의 onMessage 메서드로 전달
	 */
	@Bean
	public MessageListenerAdapter listenerAdapter() {
		return new MessageListenerAdapter(redisMessageSubscriber, "onMessage");
	}

	/**
	 * Redis 메시지 리스너 컨테이너 설정
	 * Redis Pub/Sub 메시지를 수신하고 처리하는 컨테이너
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		MessageListenerAdapter listenerAdapter
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		// 채팅방 관련 모든 채널 구독 (chat:room:*)
		container.addMessageListener(listenerAdapter, Topic.pattern("chat:room:*"));

		return container;
	}


}
