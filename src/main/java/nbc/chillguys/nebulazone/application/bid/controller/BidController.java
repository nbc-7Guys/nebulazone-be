package nbc.chillguys.nebulazone.application.bid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidService;
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
}
