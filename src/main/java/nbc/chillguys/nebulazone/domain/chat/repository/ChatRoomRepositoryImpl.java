package nbc.chillguys.nebulazone.domain.chat.repository;

import static nbc.chillguys.nebulazone.domain.chat.entity.QChatRoom.*;
import static nbc.chillguys.nebulazone.domain.chat.entity.QChatRoomUser.*;
import static nbc.chillguys.nebulazone.domain.product.entity.QProduct.*;
import static nbc.chillguys.nebulazone.domain.user.entity.QUser.*;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<ChatRoomInfo> findAllByUserId(Long userId) {
		return jpaQueryFactory
			.select(Projections.constructor(
				ChatRoomInfo.class,
				product.name,
				user.nickname,
				chatRoom.id
			))
			.from(chatRoom)
			.join(chatRoomUser).on(chatRoomUser.chatRoom.eq(chatRoom))
			.join(chatRoom.product, product)
			.join(product.seller, user)
			.where(chatRoomUser.user.id.eq(userId))
			.fetch();
	}
}
