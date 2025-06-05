package nbc.chillguys.nebulazone.application.bid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@RestController
@RequiredArgsConstructor
public class BidController {

	private final BidService bidService;

	@PostMapping("/auctions/{auctionId}/bids")
	public ResponseEntity<CreateBidResponse> createBid(
		@PathVariable("auctionId") Long auctionId,
		@AuthenticationPrincipal AuthUser authUser,
		@Valid @RequestBody CreateBidRequest request) {

		CreateBidResponse response = bidService.createBid(auctionId, authUser, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/autioncs/{auctionId}/bids")
	public ResponseEntity<CommonPageResponse<FindBidResponse>> findBids(
		@PathVariable("auctionId") Long auctionId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "20") int size) {

		CommonPageResponse<FindBidResponse> response = bidService.findBids(auctionId, Math.max(page - 1, 0), size);

		return ResponseEntity.ok(response);
	}
}
