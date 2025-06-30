package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record EndAuctionResponse(
	Long auctionId,
	Long winnerId,
	String winnerNickname,
	String winnerEmail,
	Long wonBidPrice,
	String wonProductName,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime wonDate
) {

	public static EndAuctionResponse of(Auction auction, BidVo wonBidVo, Product product) {
		return new EndAuctionResponse(
			auction.getId(),
			wonBidVo.getBidUserId(),
			wonBidVo.getBidUserNickname(),
			wonBidVo.getBidUserEmail(),
			auction.getCurrentPrice(),
			product.getName(),
			auction.getModifiedAt()
		);
	}
}
