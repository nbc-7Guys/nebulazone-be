package nbc.chillguys.nebulazone.application.bid.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.bid.dto.request.BidAdminSearchRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.BidAdminResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;

@RestController
@RequestMapping("/admin/bids")
@RequiredArgsConstructor
public class BidAdminController {
	private final BidAdminService bidAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<BidAdminResponse>> findBids(
		@RequestParam(value = "auctionId", required = false) Long auctionId,
		@RequestParam(value = "userId", required = false) Long userId,
		@RequestParam(value = "status", required = false) BidStatus status,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		BidAdminSearchRequest request = new BidAdminSearchRequest(auctionId, userId, status, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<BidAdminResponse> response = bidAdminService.findBids(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{bidId}/status")
	public ResponseEntity<Void> updateBidStatus(
		@PathVariable Long bidId,
		@RequestBody BidStatus status
	) {
		bidAdminService.updateBidStatus(bidId, status);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{bidId}")
	public ResponseEntity<Void> cancelStatusBid(@PathVariable Long bidId) {
		bidAdminService.cancelStatusBid(bidId);
		return ResponseEntity.noContent().build();
	}
}
