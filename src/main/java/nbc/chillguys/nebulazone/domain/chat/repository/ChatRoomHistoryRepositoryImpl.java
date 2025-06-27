package nbc.chillguys.nebulazone.domain.chat.repository;

import static nbc.chillguys.nebulazone.domain.chat.entity.QChatHistory.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;

@RequiredArgsConstructor
public class ChatRoomHistoryRepositoryImpl implements ChatRoomHistoryRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Slice<ChatHistory> findAllByChatRoomIdOrderBySendTimeAsc(Long chatRoomId, Long lastId, Pageable pageable) {
		List<ChatHistory> results = jpaQueryFactory
			.selectFrom(chatHistory)
			.where(
				chatHistory.chatRoom.id.eq(chatRoomId),
				ltLastId(lastId)
			)
			.orderBy(chatHistory.sendTime.asc())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		return checkNextAndCreateSlice(results, pageable);
	}

	private BooleanExpression ltLastId(Long lastId) {
		if (lastId == null) {
			return null;
		}
		return chatHistory.id.lt(lastId);
	}

	private Slice<ChatHistory> checkNextAndCreateSlice(List<ChatHistory> results, Pageable pageable) {
		boolean hasNext = false;

		if (results.size() > pageable.getPageSize()) {
			hasNext = true;
			results.remove(pageable.getPageSize());
		}
		return new SliceImpl<>(results, pageable, hasNext);
	}
}
