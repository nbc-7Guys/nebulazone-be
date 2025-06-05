package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

	private final BidDomainService bidDomainService;
	private final UserDomainService userDomainService;
	private final AuctionDomainService auctionDomainService;

	public CreateBidResponse createBid(Long auctionId, AuthUser authUser, CreateBidRequest request) {

		Auction auction = auctionDomainService.findAuctionById(auctionId);

		User user = userDomainService.findActiveUserById(authUser.getId());

		Bid createBid = bidDomainService.createBid(auction, user, request.price());
		return CreateBidResponse.from(createBid);

	}
}
