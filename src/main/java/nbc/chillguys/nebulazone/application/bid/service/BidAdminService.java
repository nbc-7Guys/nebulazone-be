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
import nbc.chillguys.nebulazone.infra.redis.lock.DistributedLock;

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
			request.auctionId(),
			request.userId(),
			request.status()
		);
		Page<BidAdminInfo> infoPage = bidAdminDomainService.findBids(command, pageable);
		return CommonPageResponse.from(infoPage.map(BidAdminResponse::from));
	}

	public void updateBidStatus(Long bidId, BidStatus status) {
		Bid bid = bidAdminDomainService.findByBidId(bidId);
		bidAdminDomainService.updateBidStatus(bidId, status);

		bidRedisService.updateBidStatusByAdmin(
			bid.getAuction().getId(),
			bid.getUser().getId(),
			bid.getPrice(),
			bid.getCreatedAt(),
			status.name()
		);
	}

	@DistributedLock(key = "#bidId")
	public void deleteBid(Long bidId) {
		Bid bid = bidAdminDomainService.findByBidId(bidId);
		bidAdminDomainService.deleteBid(bidId);

		bidRedisService.deleteBidByAdmin(
			bid.getAuction().getId(),
			bid.getUser().getId(),
			bid.getPrice(),
			bid.getCreatedAt()
		);
	}
}
