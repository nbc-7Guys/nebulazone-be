package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.BidAdminSearchRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.BidAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.BidAdminDomainService;

@Service
@RequiredArgsConstructor
public class BidAdminService {
	private final BidAdminDomainService bidAdminDomainService;

	public CommonPageResponse<BidAdminResponse> findBids(BidAdminSearchRequest request, Pageable pageable) {
		BidAdminSearchQueryCommand command = new BidAdminSearchQueryCommand(
			request.auctionId(),
			request.userId(),
			request.status()
		);
		Page<BidAdminInfo> infoPage = bidAdminDomainService.findBids(command, pageable);
		return CommonPageResponse.from(infoPage.map(BidAdminResponse::from));
	}

	public void updateBidStatus(Long bidId, BidStatus status) {
		bidAdminDomainService.updateBidStatus(bidId, status);
	}

	public void deleteBid(Long bidId) {
		bidAdminDomainService.deleteBid(bidId);
	}
}
