package nbc.chillguys.nebulazone.application.auction.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.service.AuctionService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

	private final AuctionService auctionService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<FindAuctionResponse>> findAuctions(
		@RequestParam(defaultValue = "1", value = "page") int page,
		@RequestParam(defaultValue = "20", value = "size") int size) {

		CommonPageResponse<FindAuctionResponse> response = auctionService.findAuctions(Math.max(page - 1, 0), size);

		return ResponseEntity.ok(response);

	}

	@GetMapping("/sorted")
	public ResponseEntity<List<FindAuctionResponse>> findAuctions(
		@RequestParam("sort") String sortType) {

		List<FindAuctionResponse> response = auctionService.findAuctionsBySortType(AuctionSortType.of(sortType));

		return ResponseEntity.ok(response);

	}
}
