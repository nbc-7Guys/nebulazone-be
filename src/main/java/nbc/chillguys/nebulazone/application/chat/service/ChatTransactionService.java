package nbc.chillguys.nebulazone.application.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;

@Service
@RequiredArgsConstructor
public class ChatTransactionService {
	private final ChatDomainService chatDomainService;

	@Transactional
	public void saveMessagesTransaction(Long roomId, List<ChatMessageInfo> messagesFromRedis) {
		chatDomainService.saveChatHistories(roomId, messagesFromRedis);
	}
}
