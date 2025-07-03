package nbc.chillguys.nebulazone.application.bid.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.BidAdminSearchRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.BidAdminResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.BidAdminDomainService;

@Service
@RequiredArgsConstructor
public class BidAdminService {
	private final BidAdminDomainService bidAdminDomainService;
	private final BidRedisService bidRedisService;

	public CommonPageResponse<BidAdminResponse> findBids(BidAdminSearchRequest request, Pageable pageable) {
		if (request.auctionId() != null) {
			Page<FindBidResponse> redisBids = bidRedisService.findBidsByAuctionId(
				request.auctionId(), pageable.getPageNumber(), pageable.getPageSize());
			List<BidAdminResponse> responses = redisBids.getContent().stream()
				.map(bid -> new BidAdminResponse(
					null,
					bid.auctionId(),
					null,
					null,
					bid.bidUserNickname(),
					bid.bidPrice(),
					bid.bidStatus(),
					bid.bidTime(),
					null
				))
				.toList();
			return CommonPageResponse.from(new PageImpl<>(responses, pageable, redisBids.getTotalElements()));
		}

		BidAdminSearchQueryCommand command = new BidAdminSearchQueryCommand(
			null,
			request.userId(),
			request.status()
		);
		Page<BidAdminInfo> infoPage = bidAdminDomainService.findBids(command, pageable);
		return CommonPageResponse.from(infoPage.map(BidAdminResponse::from));
	}

	public void updateBidStatus(Long bidId, BidStatus status) {
		Bid bid = bidAdminDomainService.findByBidId(bidId);
		bidAdminDomainService.updateBidStatus(bidId, status);

		bidRedisService.updateBidByAdmin(
			bid.getAuction().getId(),
			bid.getUser().getId(),
			bid.getPrice(),
			bid.getCreatedAt(),
			status.name()
		);
	}

	public void cancelStatusBid(Long bidId) {
		Bid bid = bidAdminDomainService.findByBidId(bidId);
		bidAdminDomainService.cancelStatusBid(bidId);

		bidRedisService.cancelBidByAdmin(
			bid.getAuction().getId(),
			bid.getUser().getId(),
			bid.getPrice(),
			bid.getCreatedAt()
		);
	}
}
