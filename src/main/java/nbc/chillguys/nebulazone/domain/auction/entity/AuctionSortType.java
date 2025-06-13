package nbc.chillguys.nebulazone.domain.auction.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;

@Getter
@RequiredArgsConstructor
public enum AuctionSortType {
	CLOSING("마감 임박순 정렬"),
	POPULAR("인기순 정렬");

	private final String description;

	public static AuctionSortType of(String sortType) {
		return Arrays.stream(AuctionSortType.values())
			.filter(type -> type.name().equalsIgnoreCase(sortType))
			.findFirst()
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.INVALID_AUCTION_SORT_TYPE));

	}
}
