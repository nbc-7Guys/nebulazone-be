package nbc.chillguys.nebulazone.domain.chat.repository;

import static nbc.chillguys.nebulazone.domain.chat.entity.QChatHistory.*;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

@RequiredArgsConstructor
public class ChatRoomHistoryRepositoryImpl implements ChatRoomHistoryRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<ChatHistory> findAllByChatRoomIdOrderBySendTimeAsc(Long chatRoomId) {
		return jpaQueryFactory
			.selectFrom(chatHistory)
			.where(
				chatHistory.chatRoom.id.eq(chatRoomId)
			)
			.orderBy(chatHistory.sendTime.asc())
			.fetch();
	}
}
