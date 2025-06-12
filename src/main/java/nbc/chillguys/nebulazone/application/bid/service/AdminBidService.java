package nbc.chillguys.nebulazone.application.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.AdminBidSearchRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.AdminBidResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.bid.dto.AdminBidInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.AdminBidSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.service.AdminBidDomainService;

@Service
@RequiredArgsConstructor
public class AdminBidService {
	private final AdminBidDomainService adminBidDomainService;

	public CommonPageResponse<AdminBidResponse> findBids(AdminBidSearchRequest request, Pageable pageable) {
		AdminBidSearchQueryCommand command = new AdminBidSearchQueryCommand(
			request.auctionId(),
			request.userId(),
			request.status()
		);
		Page<AdminBidInfo> infoPage = adminBidDomainService.findBids(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminBidResponse::from));
	}

	public void updateBidStatus(Long bidId, BidStatus status) {
		adminBidDomainService.updateBidStatus(bidId, status);
	}

	public void deleteBid(Long bidId) {
		adminBidDomainService.deleteBid(bidId);
	}
}
