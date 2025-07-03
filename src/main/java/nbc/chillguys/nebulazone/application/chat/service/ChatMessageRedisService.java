package nbc.chillguys.nebulazone.application.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;

@Service
@RequiredArgsConstructor
public class ChatMessageRedisService {

	private static final String CHAT_MESSAGE_KEY_PREFIX = "chat:message:";
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * 채팅 메시지를 Redis에 비동기로 저장
	 *
	 * <p>채팅방별로 메시지를 List 구조로 저장하여 순서를 보장</p>
	 *
	 * @param roomId 채팅방 ID
	 * @param messageInfo 저장할 채팅 메시지 정보
	 * @throws RuntimeException Redis 저장 중 오류 발생 시
	 * @author 박형우
	 */
	@Async
	public void saveMessageToRedis(Long roomId, ChatMessageInfo messageInfo) {
		try {
			String key = CHAT_MESSAGE_KEY_PREFIX + roomId;
			redisTemplate.opsForList().rightPush(key, messageInfo);
		} catch (Exception e) {
			throw new RuntimeException("Redis에 저장 중 오류 발생", e);
		}
	}

	/**
	 * 채팅방의 모든 메시지를 Redis에서 조회
	 *
	 * <p>채팅방별로 저장된 모든 메시지를 시간순으로 반환</p>
	 *
	 * @param roomId 조회할 채팅방 ID
	 * @return 채팅방의 모든 메시지 목록 (시간순)
	 * @author 박형우
	 */
	public List<ChatMessageInfo> getMessagesFromRedis(Long roomId) {
		String key = CHAT_MESSAGE_KEY_PREFIX + roomId;

		List<Object> raw = Optional.ofNullable(redisTemplate.opsForList().range(key, 0, -1)).orElse(List.of());

		return raw.stream()
			.map(o -> objectMapper.convertValue(o,
				ChatMessageInfo.class)) // ObjectMapper.convertValue를 통해 ChatMessageInfo객체로 변환
			.toList();
	}

	/**
	 * 채팅방의 모든 메시지를 Redis에서 삭제
	 *
	 * <p>채팅방이 종료되거나 정리가 필요할 때 해당 채팅방의 모든 캐시된 메시지를 삭제</p>
	 *
	 * @param roomId 메시지를 삭제할 채팅방 ID
	 * @author 박형우
	 */
	public void deleteMessagesInRedis(Long roomId) {
		redisTemplate.delete(CHAT_MESSAGE_KEY_PREFIX + roomId);
	}

}
