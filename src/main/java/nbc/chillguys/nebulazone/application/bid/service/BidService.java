package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidDomainService bidDomainService;
	private final AuctionDomainService auctionDomainService;

	public CommonPageResponse<FindBidResponse> findBids(Long auctionId, int page, int size) {
		Auction auction = auctionDomainService.findActiveAuctionById(auctionId);

		Page<FindBidInfo> findBids = bidDomainService.findBids(auction, page, size);
		Page<FindBidResponse> response = findBids.map(FindBidResponse::from);

		return CommonPageResponse.from(response);
	}

	public CommonPageResponse<FindBidResponse> findMyBids(User user, int page, int size) {
		Page<FindBidInfo> findBids = bidDomainService.findMyBids(user, page, size);
		Page<FindBidResponse> response = findBids.map(FindBidResponse::from);

		return CommonPageResponse.from(response);
	}

	@Transactional
	public DeleteBidResponse statusBid(User user, Long auctionId, Long bidId) {
		Auction auction = auctionDomainService.findActiveAuctionWithProductAndSellerLock(auctionId);

		Long deletedBidId = bidDomainService.statusBid(auction, user, bidId);
		return DeleteBidResponse.from(deletedBidId);

	}
}
