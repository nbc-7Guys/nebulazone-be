package nbc.chillguys.nebulazone.application.auction.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidRedisService;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionDomainService auctionDomainService;
	private final BidDomainService bidDomainService;

	private final AuctionRedisService auctionRedisService;
	private final BidRedisService bidRedisService;

	public FindDetailAuctionResponse findAuction(Long auctionId) {
		AuctionVo auctionVo = auctionRedisService.findRedisAuctionVo(auctionId);

		if (auctionVo != null) {
			Long bidCount = auctionRedisService.calculateAuctionBidCount(auctionId);
			BidVo bidVo = bidRedisService.getBidVo(auctionId, auctionVo.getCurrentPrice());
			return FindDetailAuctionResponse.of(auctionVo, bidVo, bidCount);
		}

		Bid highestPriceBid = bidDomainService.findHighBidByAuction(auctionId);
		AuctionFindDetailInfo auctionFindDetailInfo = auctionDomainService.findAuction(auctionId);

		return FindDetailAuctionResponse.of(auctionFindDetailInfo, highestPriceBid);
	}

}
