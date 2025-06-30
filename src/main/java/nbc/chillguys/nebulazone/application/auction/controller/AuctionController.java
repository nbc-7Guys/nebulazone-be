package nbc.chillguys.nebulazone.application.auction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindSortTypeAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.auction.service.AuctionService;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionController {

	private final AuctionService auctionService;
	private final AuctionRedisService auctionRedisService;

	@GetMapping("/sorted")
	public ResponseEntity<FindSortTypeAuctionResponse> findAuctionsSortType(
		@RequestParam("sort") String sortType) {

		AuctionSortType auctionSortType = AuctionSortType.of(sortType);
		FindSortTypeAuctionResponse response = auctionRedisService.findAuctionsBySortType(auctionSortType);

		return ResponseEntity.ok(response);

	}

	@GetMapping("/{auctionId}")
	public ResponseEntity<FindDetailAuctionResponse> findAuction(@PathVariable("auctionId") Long auctionId) {

		FindDetailAuctionResponse response = auctionService.findAuction(auctionId);

		return ResponseEntity.ok(response);

	}

	@PostMapping("/{auctionId}")
	public ResponseEntity<EndAuctionResponse> manualEndAuction(
		@PathVariable("auctionId") Long auctionId,
		@AuthenticationPrincipal User user,
		@Valid @RequestBody ManualEndAuctionRequest request) {

		EndAuctionResponse response = auctionRedisService.manualEndAuction(auctionId, user, request);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{auctionId}")
	public ResponseEntity<DeleteAuctionResponse> deleteAuction(
		@PathVariable("auctionId") Long auctionId,
		@AuthenticationPrincipal User user) {

		DeleteAuctionResponse response = auctionRedisService.deleteAuction(auctionId, user);

		return ResponseEntity.ok(response);
	}
}
