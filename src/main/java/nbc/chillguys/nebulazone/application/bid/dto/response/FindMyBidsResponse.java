package nbc.chillguys.nebulazone.application.bid.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

public record FindMyBidsResponse(
	Long bidUserId,
	String bidUserNickname,
	String bidStatus,
	Long bidPrice,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime bidTime,
	Long auctionId,
	Long productId,
	String auctionProductName
) {

	public static FindMyBidsResponse from(FindMyBidsInfo findMyBidsInfo) {
		return new FindMyBidsResponse(
			findMyBidsInfo.bidUserId(),
			findMyBidsInfo.bidUserNickname(),
			findMyBidsInfo.bidStatus().name(),
			findMyBidsInfo.bidPrice(),
			findMyBidsInfo.bidTime(),
			findMyBidsInfo.auctionId(),
			findMyBidsInfo.productId(),
			findMyBidsInfo.auctionProductName()
		);
	}

	public static FindMyBidsResponse of(BidVo bidVo, AuctionVo auctionVo) {
		return new FindMyBidsResponse(
			bidVo.getBidUserId(),
			bidVo.getBidUserNickname(),
			bidVo.getBidStatus(),
			bidVo.getBidPrice(),
			bidVo.getBidCreatedAt(),
			bidVo.getAuctionId(),
			auctionVo.getProductId(),
			auctionVo.getProductName()
		);
	}

}
