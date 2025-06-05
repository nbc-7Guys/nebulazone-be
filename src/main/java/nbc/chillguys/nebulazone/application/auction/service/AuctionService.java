package nbc.chillguys.nebulazone.application.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAuctionResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

	private final AuctionDomainService auctionDomainService;

	public CommonPageResponse<FindAuctionResponse> findAuctions(int page, int size) {

		Page<AuctionFindInfo> findAuctions = auctionDomainService.findAuctions(page, size);
		Page<FindAuctionResponse> response = findAuctions.map(FindAuctionResponse::from);

		return CommonPageResponse.from(response);
	}

	public List<FindAuctionResponse> findAuctionsBySortType(AuctionSortType sortType) {

		List<AuctionFindInfo> findAuctionsBySortType = auctionDomainService.findAuctionsBySortType(sortType);

		return findAuctionsBySortType.stream().map(FindAuctionResponse::from).toList();
	}
}
