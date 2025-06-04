package nbc.chillguys.nebulazone.application.chat.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;

@Service
@RequiredArgsConstructor
public class ChatMessageRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private static final String CHAT_MESSAGE_KEY_PREFIX = "chat:message:";

	/**
     * 채팅 메시지 Redis에 저장
     */
	public void saveMessageToRedis(Long roomId, ChatMessageInfo messageInfo) {
		try {
			String key = CHAT_MESSAGE_KEY_PREFIX + roomId;
			redisTemplate.opsForList().rightPush(key, messageInfo);
		} catch (Exception e) {
			throw new RuntimeException("Redis에 저장 중 오류 발생", e);
		}
	}

	/**
     * 채팅방 메시지 전체 조회
     */
    public List<ChatMessageInfo> getMessagesFromRedis(Long roomId) {
        String key = CHAT_MESSAGE_KEY_PREFIX + roomId;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);

        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        return raw.stream()
            .filter(ChatMessageInfo.class::isInstance)
            .map(ChatMessageInfo.class::cast)
            .toList();
    }

	/**
     * 채팅방 메시지 삭제
     */
    public void deleteMessagesInRedis(Long roomId) {
        redisTemplate.delete(CHAT_MESSAGE_KEY_PREFIX + roomId);
    }

}
