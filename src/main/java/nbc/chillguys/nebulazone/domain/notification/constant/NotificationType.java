package nbc.chillguys.nebulazone.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	CHAT_ROOM_CREATED("새 채팅방"),
	AUCTION_BID("경매 입찰"),
	AUCTION_END("경매 종료"),
	AUCTION_WIN("경매 낙찰"),
	PRODUCT_PURCHASE("상품 구매"),
	SYSTEM_NOTICE("시스템 공지");

	private final String description;
}
