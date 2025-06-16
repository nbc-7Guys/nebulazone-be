package nbc.chillguys.nebulazone.application.pointhistory.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.service.PointHistoryAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

@RestController
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointHistoryAdminController {
	private final PointHistoryAdminService pointHistoryAdminService;

	@GetMapping("/histories")
	public ResponseEntity<CommonPageResponse<AdminPointHistoryResponse>> getAdminPointHistories(
		@RequestParam(value = "email", required = false) String email,
		@RequestParam(value = "nickname", required = false) String nickname,
		@RequestParam(value = "type", required = false) PointHistoryType type,
		@RequestParam(value = "status", required = false) PointHistoryStatus status,
		@RequestParam(value = "startDate", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
		@RequestParam(value = "endDate", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "20") int size) {
		PointHistoryAdminRequest request = new PointHistoryAdminRequest(email, nickname, type, status, startDate,
			endDate);

		CommonPageResponse<AdminPointHistoryResponse> response = pointHistoryAdminService.searchAdminPointHistories(
			request, PageRequest.of(page - 1, size));

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/points/{pointHistoryId}/approve")
	public ResponseEntity<Void> approvePointRequest(@PathVariable Long pointHistoryId) {
		pointHistoryAdminService.approvePointHistory(pointHistoryId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/points/{pointHistoryId}/reject")
	public ResponseEntity<Void> rejectPointRequest(@PathVariable Long pointHistoryId) {
		pointHistoryAdminService.rejectPointHistory(pointHistoryId);
		return ResponseEntity.ok().build();
	}

}
