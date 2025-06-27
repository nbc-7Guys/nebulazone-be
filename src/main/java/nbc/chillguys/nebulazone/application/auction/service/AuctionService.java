package nbc.chillguys.nebulazone.application.auction.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAllAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionDomainService auctionDomainService;
	private final BidDomainService bidDomainService;
	private final ProductDomainService productDomainService;

	public CommonPageResponse<FindAllAuctionResponse> findAuctions(int page, int size) {

		Page<AuctionFindAllInfo> findAuctions = auctionDomainService.findAuctions(page, size);
		Page<FindAllAuctionResponse> response = findAuctions.map(FindAllAuctionResponse::from);

		return CommonPageResponse.from(response);
	}

	public FindDetailAuctionResponse findAuction(Long auctionId) {

		Bid highestPriceBid = bidDomainService.findHighBidByAuction(auctionId);
		AuctionFindDetailInfo auctionFindDetailInfo = auctionDomainService.findAuction(auctionId);

		return FindDetailAuctionResponse.from(auctionFindDetailInfo, highestPriceBid);
	}

	// @Transactional
	// public DeleteAuctionResponse deleteAuction(Long auctionId, User user) {
	//
	// 	Auction deletedAuction = auctionDomainService.deleteAuction(auctionId, user);
	//
	// 	Product product = deletedAuction.getProduct();
	// 	product.delete();
	// 	productDomainService.deleteProductFromEs(product.getId());
	//
	// 	return DeleteAuctionResponse.from(deletedAuction.getId(), product.getId());
	// }

}
