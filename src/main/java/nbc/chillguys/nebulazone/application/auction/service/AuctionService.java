package nbc.chillguys.nebulazone.application.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAllAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.ManualEndAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.ManualEndAuctionInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.service.TransactionDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionDomainService auctionDomainService;
	private final AuctionSchedulerService auctionSchedulerService;
	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;
	private final TransactionDomainService txDomainService;
	private final ProductDomainService productDomainService;

	public CommonPageResponse<FindAllAuctionResponse> findAuctions(int page, int size) {

		Page<AuctionFindAllInfo> findAuctions = auctionDomainService.findAuctions(page, size);
		Page<FindAllAuctionResponse> response = findAuctions.map(FindAllAuctionResponse::from);

		return CommonPageResponse.from(response);
	}

	public List<FindAllAuctionResponse> findAuctionsBySortType(AuctionSortType sortType) {

		List<AuctionFindAllInfo> findAuctionsBySortType = auctionDomainService.findAuctionsBySortType(sortType);

		return findAuctionsBySortType.stream().map(FindAllAuctionResponse::from).toList();
	}

	@Transactional
	public DeleteAuctionResponse deleteAuction(Long auctionId, AuthUser authUser) {

		User user = userDomainService.findActiveUserById(authUser.getId());
		Long deletedAuctionId = auctionDomainService.deleteAuction(auctionId, user);
		auctionSchedulerService.cancelSchedule(deletedAuctionId);

		return DeleteAuctionResponse.from(deletedAuctionId);
	}

	@Transactional
	public ManualEndAuctionResponse manualEndAuction(Long auctionId, AuthUser authUser,
		ManualEndAuctionRequest request) {

		User loginUser = userDomainService.findActiveUserById(authUser.getId());
		Bid wonBid = bidDomainService.findBid(request.bidId());
		Product product = productDomainService.findActiveProductById(request.productId());
		product.purchase();

		ManualEndAuctionInfo auctionInfo = auctionDomainService.manualEndAuction(loginUser, wonBid, auctionId);

		TransactionCreateCommand txCreateCommand = TransactionCreateCommand.of(wonBid.getUser(), product,
			product.getTxMethod().name(), auctionInfo.wonProductPrice());

		txDomainService.createTransaction(txCreateCommand);

		return ManualEndAuctionResponse.from(auctionInfo);
	}

	public FindDetailAuctionResponse findAuction(Long auctionId) {

		Bid highestPriceBid = bidDomainService.findHighBidByAuction(auctionId);
		AuctionFindDetailInfo auctionFindDetailInfo = auctionDomainService.findAuction(auctionId);

		return FindDetailAuctionResponse.from(auctionFindDetailInfo, highestPriceBid);
	}

}
