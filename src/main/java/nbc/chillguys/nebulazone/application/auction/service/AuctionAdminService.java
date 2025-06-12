package nbc.chillguys.nebulazone.application.auction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.AuctionAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionAdminDomainService;

@Service
@RequiredArgsConstructor
public class AuctionAdminService {
	private final AuctionAdminDomainService auctionAdminDomainService;

	public CommonPageResponse<AuctionAdminResponse> findAuctions(AuctionAdminSearchRequest request, Pageable pageable) {
		AuctionAdminSearchQueryCommand command = new AuctionAdminSearchQueryCommand(
			request.keyword(),
			request.deleted(),
			request.isWon()
		);
		Page<AuctionAdminInfo> infoPage = auctionAdminDomainService.findAuctions(command, pageable);
		return CommonPageResponse.from(infoPage.map(AuctionAdminResponse::from));
	}

	public void updateAuction(Long auctionId, AuctionAdminUpdateRequest request) {
		AuctionAdminUpdateCommand command = AuctionAdminUpdateCommand.from(request);
		auctionAdminDomainService.updateAuction(auctionId, command);
	}

	public void deleteAuction(Long auctionId) {
		auctionAdminDomainService.deleteAuction(auctionId);
	}

	public void restoreAuction(Long auctionId) {
		auctionAdminDomainService.restoreAuction(auctionId);
	}

}
