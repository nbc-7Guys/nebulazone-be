package nbc.chillguys.nebulazone.application.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class AuctionService {

	private final AuctionDomainService auctionDomainService;
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

	public DeleteAuctionResponse deleteAuction(long auctionId, AuthUser authUser) {

		User user = userDomainService.findActiveUserById(authUser.getId());
		Long deletedAuctionId = auctionDomainService.deleteAuction(auctionId, user);

		return DeleteAuctionResponse.from(deletedAuctionId);
	}
}
