package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.metrics.BidMetrics;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;
	private final AuctionDomainService auctionDomainService;
	private final BidMetrics bidMetrics;

	/**
	 * 경매에 기존 입찰 내역이 없다면 입찰 생성, 있다면 입찰 수정
	 * @param auctionId 대상 경매
	 * @param loggedInUser 로그인 유저
	 * @param request 입찰 정보
	 * @return 입찰 후 반환값
	 * @author 전나겸
	 */
	@Transactional
	public CreateBidResponse upsertBid(Long auctionId, User loggedInUser, CreateBidRequest request) {
		long start = System.currentTimeMillis();

		try {
			Auction lockAuction = auctionDomainService.findActiveAuctionWithProductAndSellerLock(auctionId);
			User user = userDomainService.findActiveUserById(loggedInUser.getId());

			Bid resultBid = bidDomainService.findBidByAuctionIdAndUserId(lockAuction.getId(), user.getId())
				.map(findBid -> bidDomainService.updateBid(lockAuction, findBid, user, request.price()))
				.orElseGet(() -> bidDomainService.createBid(lockAuction, user, request.price()));

			bidMetrics.countBidSuccess();
			bidMetrics.recordBidAmount(request.price());

			return CreateBidResponse.from(resultBid);

		} finally {
			long elapsed = System.currentTimeMillis() - start;
			bidMetrics.recordBidLatency(elapsed);
		}
	}

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
