package nbc.chillguys.nebulazone.application.bid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindMyBidsResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidRedisService;
import nbc.chillguys.nebulazone.application.bid.service.BidService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@RestController
@RequiredArgsConstructor
public class BidController {

	private final BidService bidService;
	private final BidRedisService bidRedisService;

	@PostMapping("/auctions/{auctionId}/bids")
	public ResponseEntity<CreateBidResponse> createBid(
		@PathVariable("auctionId") Long auctionId,
		@AuthenticationPrincipal User user,
		@Valid @RequestBody CreateBidRequest request) {

		CreateBidResponse response = bidRedisService.createBid(auctionId, user, request.price());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/auctions/{auctionId}/bids")
	public ResponseEntity<CommonPageResponse<FindBidResponse>> findBidsByAuctionId(
		@PathVariable("auctionId") Long auctionId,
		@RequestParam(defaultValue = "1", value = "page") int page,
		@RequestParam(defaultValue = "20", value = "size") int size) {

		CommonPageResponse<FindBidResponse> response = bidService.findBidsByAuctionId(auctionId, toZeroBasedPage(page),
			size);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/bids/me")
	public ResponseEntity<CommonPageResponse<FindMyBidsResponse>> findMyBids(
		@AuthenticationPrincipal User user,
		@RequestParam(defaultValue = "1", value = "page") int page,
		@RequestParam(defaultValue = "20", value = "size") int size) {

		CommonPageResponse<FindMyBidsResponse> response = bidService.findMyBids(user, toZeroBasedPage(page), size);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/auctions/{auctionId}/bids/{bidPrice}")
	public ResponseEntity<DeleteBidResponse> statusBid(
		@AuthenticationPrincipal User user,
		@PathVariable("auctionId") Long auctionId,
		@PathVariable("bidPrice") Long bidPrice
	) {

		DeleteBidResponse response = bidRedisService.statusBid(user, auctionId, bidPrice);

		return ResponseEntity.ok(response);
	}

	private int toZeroBasedPage(int page) {
		return Math.max(page - 1, 0);
	}
}
