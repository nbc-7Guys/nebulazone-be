package nbc.chillguys.nebulazone.infra.redis.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;

public record FindAllAuctionsDto(
	Long auctionId,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	LocalDateTime endTime,
	LocalDateTime createdAt,
	Long productId,
	String productName,
	String productImageUrl,
	Long bidCount
) {

	public static FindAllAuctionsDto of(AuctionVo auctionVo, Long bidCount) {

		String productImageUrl = Optional.ofNullable(auctionVo.getProductImageUrls())
			.filter(images -> !images.isEmpty())
			.map(List::getFirst)
			.orElse(null);

		return new FindAllAuctionsDto(
			auctionVo.getAuctionId(),
			auctionVo.getStartPrice(),
			auctionVo.getCurrentPrice(),
			auctionVo.isWon(),
			auctionVo.getEndTime(),
			auctionVo.getCreateAt(),
			auctionVo.getProductId(),
			auctionVo.getProductName(),
			productImageUrl,
			bidCount
		);

	}
}
