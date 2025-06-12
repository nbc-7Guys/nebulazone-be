package nbc.chillguys.nebulazone.application.auction.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminSearchRequest;
import nbc.chillguys.nebulazone.application.auction.dto.request.AuctionAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.AuctionAdminResponse;
import nbc.chillguys.nebulazone.application.auction.service.AuctionAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;

@RestController
@RequestMapping("/admin/auctions")
@RequiredArgsConstructor
public class AuctionAdminController {
	private final AuctionAdminService auctionAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AuctionAdminResponse>> findAuctions(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "deleted", required = false) Boolean deleted,
		@RequestParam(value = "isWon", required = false) Boolean isWon,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AuctionAdminSearchRequest request = new AuctionAdminSearchRequest(keyword, deleted, isWon, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AuctionAdminResponse> response = auctionAdminService.findAuctions(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{auctionId}")
	public ResponseEntity<Void> updateAuction(
		@PathVariable Long auctionId,
		@RequestBody @Valid AuctionAdminUpdateRequest request
	) {
		auctionAdminService.updateAuction(auctionId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{auctionId}")
	public ResponseEntity<Void> deleteAuction(@PathVariable Long auctionId) {
		auctionAdminService.deleteAuction(auctionId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{auctionId}/restore")
	public ResponseEntity<Void> restoreAuction(@PathVariable Long auctionId) {
		auctionAdminService.restoreAuction(auctionId);
		return ResponseEntity.noContent().build();
	}
}
