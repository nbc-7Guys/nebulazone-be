package nbc.chillguys.nebulazone.application.auction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.AdminAuctionSearchRequest;
import nbc.chillguys.nebulazone.application.auction.dto.request.AdminAuctionUpdateRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.AdminAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AdminAuctionInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AdminAuctionSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AdminAuctionUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.service.AdminAuctionDomainService;

@Service
@RequiredArgsConstructor
public class AdminAuctionService {
	private final AdminAuctionDomainService adminAuctionDomainService;

	public CommonPageResponse<AdminAuctionResponse> findAuctions(AdminAuctionSearchRequest request, Pageable pageable) {
		AdminAuctionSearchQueryCommand command = new AdminAuctionSearchQueryCommand(
			request.keyword(),
			request.deleted(),
			request.isWon()
		);
		Page<AdminAuctionInfo> infoPage = adminAuctionDomainService.findAuctions(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminAuctionResponse::from));
	}

	public void updateAuction(Long auctionId, AdminAuctionUpdateRequest request) {
		AdminAuctionUpdateCommand command = AdminAuctionUpdateCommand.from(request);
		adminAuctionDomainService.updateAuction(auctionId, command);
	}

	public void deleteAuction(Long auctionId) {
		adminAuctionDomainService.deleteAuction(auctionId);
	}

	public void restoreAuction(Long auctionId) {
		adminAuctionDomainService.restoreAuction(auctionId);
	}

}
