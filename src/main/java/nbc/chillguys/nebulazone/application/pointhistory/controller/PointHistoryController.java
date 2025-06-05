package nbc.chillguys.nebulazone.application.pointhistory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointResponse;
import nbc.chillguys.nebulazone.application.pointhistory.service.PointHistoryService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointHistoryController {

	private final PointHistoryService pointHistoryService;

	@PostMapping("/funds")
	public ResponseEntity<PointResponse> createPointHistory(
		@RequestBody @Valid PointRequest request,
		@AuthenticationPrincipal(expression = "id") Long userId
	) {
		PointResponse response = pointHistoryService.createPointHistory(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/requests")
	public ResponseEntity<List<PointHistoryResponse>> getMyPointRequests(
		@AuthenticationPrincipal(expression = "id") Long userId,
		@RequestParam(required = false) PointHistoryStatus status
	) {
		List<PointHistoryResponse> responseList = pointHistoryService.findMyPointRequests(userId, status);
		return ResponseEntity.status(HttpStatus.OK).body(responseList);
	}

	@GetMapping("/histories")
	public ResponseEntity<CommonPageResponse<PointHistoryResponse>> getMyPointHistories(
		@AuthenticationPrincipal(expression = "id") Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		CommonPageResponse<PointHistoryResponse> response = pointHistoryService.findMyPointHistories(userId, page,
			size);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping("/points/{pointId}")
	public ResponseEntity<Void> rejectPointRequest(
		@PathVariable Long pointId,
		@AuthenticationPrincipal(expression = "id") Long userId
	) {
		pointHistoryService.rejectPointRequest(userId, pointId);
		return ResponseEntity.ok().build();
	}

}
