package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;

public record FindSortTypeAuctionResponse(
	List<AuctionSortTypeInfo> auctions,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime lastUpdated
) {

	record AuctionSortTypeInfo(
		Long auctionId,
		Long startPrice,
		Long currentPrice,
		boolean isWon,
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		LocalDateTime endTime,
		Long productId,
		String productName,
		String productImageUrl,
		Long bidCount
	) {
		private static AuctionSortTypeInfo from(AuctionFindAllInfo findInfo) {
			return new AuctionSortTypeInfo(
				findInfo.auctionId(),
				findInfo.startPrice(),
				findInfo.currentPrice(),
				findInfo.isWon(),
				findInfo.endTime(),
				findInfo.productId(),
				findInfo.productName(),
				findInfo.productImageUrl() != null ? findInfo.productImageUrl() : "이미지 없음",
				findInfo.bidCount()
			);
		}
	}

	public static FindSortTypeAuctionResponse from(List<AuctionFindAllInfo> findAllInfos) {
		List<AuctionSortTypeInfo> auctionsBySortType = findAllInfos.stream()
			.map(AuctionSortTypeInfo::from)
			.toList();

		return new FindSortTypeAuctionResponse(auctionsBySortType, LocalDateTime.now());
	}
}
