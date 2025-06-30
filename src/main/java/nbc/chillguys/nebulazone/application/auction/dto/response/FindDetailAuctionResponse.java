package nbc.chillguys.nebulazone.application.auction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record FindDetailAuctionResponse(
	Long auctionId,
	Long sellerId,
	String sellerNickname,
	String sellerEmail,
	Long bidUserId,
	String bidUserNickname,
	String bidUserEmail,
	Long startPrice,
	Long currentPrice,
	boolean isWon,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime endTime,
	Long productId,
	String productName,
	List<String> productImageUrls,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime productCreatedAt,
	Long bidCount) {

	public static FindDetailAuctionResponse of(AuctionFindDetailInfo findInfo, Bid bid) {

		return new FindDetailAuctionResponse(
			findInfo.auctionId(),
			findInfo.sellerId(),
			findInfo.sellerNickname(),
			findInfo.sellerEmail(),
			bid == null ? null : bid.getUser().getId(),
			bid == null ? null : bid.getUser().getNickname(),
			bid == null ? null : bid.getUser().getEmail(),
			findInfo.startPrice(),
			findInfo.currentPrice(),
			findInfo.isWon(),
			findInfo.endTime(),
			findInfo.productId(),
			findInfo.productName(),
			findInfo.productImageUrl(),
			findInfo.createdAt(),
			findInfo.bidCount()
		);
	}

	public static FindDetailAuctionResponse of(AuctionVo auctionVo, BidVo bidVo, Long bidCount) {

		return new FindDetailAuctionResponse(
			auctionVo.getAuctionId(),
			auctionVo.getSellerId(),
			auctionVo.getSellerNickname(),
			auctionVo.getSellerEmail(),
			bidVo == null ? null : bidVo.getBidUserId(),
			bidVo == null ? null : bidVo.getBidUserNickname(),
			bidVo == null ? null : bidVo.getBidUserEmail(),
			auctionVo.getStartPrice(),
			auctionVo.getCurrentPrice(),
			auctionVo.isWon(),
			auctionVo.getEndTime(),
			auctionVo.getProductId(),
			auctionVo.getProductName(),
			auctionVo.getProductImageUrls(),
			auctionVo.getCreateAt(),
			bidCount
		);
	}
}
