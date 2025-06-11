package nbc.chillguys.nebulazone.application.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.ManualEndAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.ManualEndAuctionInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionDomainService auctionDomainService;
	private final AuctionSchedulerService auctionSchedulerService;
	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;

	public CommonPageResponse<FindAuctionResponse> findAuctions(int page, int size) {

		Page<AuctionFindInfo> findAuctions = auctionDomainService.findAuctions(page, size);
		Page<FindAuctionResponse> response = findAuctions.map(FindAuctionResponse::from);

		return CommonPageResponse.from(response);
	}

	public List<FindAuctionResponse> findAuctionsBySortType(AuctionSortType sortType) {

		List<AuctionFindInfo> findAuctionsBySortType = auctionDomainService.findAuctionsBySortType(sortType);

		return findAuctionsBySortType.stream().map(FindAuctionResponse::from).toList();
	}

	@Transactional
	public DeleteAuctionResponse deleteAuction(Long auctionId, AuthUser authUser) {

		User user = userDomainService.findActiveUserById(authUser.getId());
		Long deletedAuctionId = auctionDomainService.deleteAuction(auctionId, user);
		auctionSchedulerService.cancelSchedule(deletedAuctionId);

		return DeleteAuctionResponse.from(deletedAuctionId);
	}

	public ManualEndAuctionResponse manualEndAuction(Long auctionId, AuthUser authUser, Long bidId) {
		User user = userDomainService.findActiveUserById(authUser.getId());
		Bid wonBid = bidDomainService.findBid(bidId);
		ManualEndAuctionInfo manualAuctionInfo = auctionDomainService.manualEndAuction(user, wonBid, auctionId);
		return ManualEndAuctionResponse.from(manualAuctionInfo);
	}
}
