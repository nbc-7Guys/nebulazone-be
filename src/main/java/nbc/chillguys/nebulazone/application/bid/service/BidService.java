package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidDomainService bidDomainService;
	private final AuctionDomainService auctionDomainService;

	private final BidRedisService bidRedisService;

	public CommonPageResponse<FindBidResponse> findBidsByAuctionId(Long auctionId, int page, int size) {

		Page<FindBidResponse> findBidResponse = bidRedisService.findBidsByAuctionId(auctionId, page, size);

		if (!findBidResponse.isEmpty()) {
			return CommonPageResponse.from(findBidResponse);
		}

		auctionDomainService.existsAuctionByIdElseThrow(auctionId);

		Page<FindBidsByAuctionInfo> findBids = bidDomainService.findBidsByAuctionId(auctionId, page, size);

		Page<FindBidResponse> response = findBids.map(FindBidResponse::from);
		return CommonPageResponse.from(response);
	}

	public CommonPageResponse<FindBidResponse> findMyBids(User user, int page, int size) {
		Page<FindMyBidsInfo> findBids = bidDomainService.findMyBids(user, page, size);
		// Page<FindBidResponse> response = findBids.map(FindBidResponse::from);

		// return CommonPageResponse.from(response);
		return null;
	}
}
