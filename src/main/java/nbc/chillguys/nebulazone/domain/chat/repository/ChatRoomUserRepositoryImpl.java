package nbc.chillguys.nebulazone.domain.chat.repository;

import static nbc.chillguys.nebulazone.domain.chat.entity.QChatRoom.*;
import static nbc.chillguys.nebulazone.domain.chat.entity.QChatRoomUser.*;
import static nbc.chillguys.nebulazone.domain.products.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.user.entity.QUser;

@RequiredArgsConstructor
public class ChatRoomUserRepositoryImpl implements ChatRoomUserRepositoryCustom{
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<ChatRoomUser> findAllByUserId(Long userId) {
		return jpaQueryFactory
			.selectFrom(chatRoomUser)
			.join(chatRoomUser.chatRoom, chatRoom).fetchJoin()
			.join(chatRoom.product, product).fetchJoin()
			.join(product.seller, user).fetchJoin()
			.where(chatRoomUser.user.id.eq(userId))
			.fetch();
	}

	@Override
	public Optional<ChatRoomUser> findByIdUserIdAndIdChatRoomId(Long userId, Long chatRoomId) {
		return Optional.ofNullable(
			jpaQueryFactory
			.selectFrom(chatRoomUser)
			.where(
				chatRoomUser.id.userId.eq(userId),
				chatRoomUser.id.chatRoomId.eq(chatRoomId)
			)
			.fetchOne()
		);
	}

	@Override
	public Optional<ChatRoomUser> findByIdUserIdAndChatRoomProductId(Long userId, Long productId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(chatRoomUser)
				.join(chatRoomUser.chatRoom, chatRoom).fetchJoin()
				.where(
					chatRoomUser.id.userId.eq(userId),
					chatRoom.product.id.eq(productId)
				)
				.fetchOne()
		);
	}
}
