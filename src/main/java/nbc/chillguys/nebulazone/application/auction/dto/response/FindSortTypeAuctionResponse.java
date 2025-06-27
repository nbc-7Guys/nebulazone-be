package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.infra.redis.dto.FindAllAuctionsDto;

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
		private static AuctionSortTypeInfo from(FindAllAuctionsDto findAllAuctionsDto) {
			return new AuctionSortTypeInfo(
				findAllAuctionsDto.auctionId(),
				findAllAuctionsDto.startPrice(),
				findAllAuctionsDto.currentPrice(),
				findAllAuctionsDto.isWon(),
				findAllAuctionsDto.endTime(),
				findAllAuctionsDto.productId(),
				findAllAuctionsDto.productName(),
				findAllAuctionsDto.productImageUrl(),
				findAllAuctionsDto.bidCount()
			);
		}
	}

	public static FindSortTypeAuctionResponse from(List<FindAllAuctionsDto> findAllAuctionsDtoList) {
		List<AuctionSortTypeInfo> auctionsBySortType = findAllAuctionsDtoList.stream()
			.map(AuctionSortTypeInfo::from)
			.toList();

		return new FindSortTypeAuctionResponse(auctionsBySortType, LocalDateTime.now());
	}
}
