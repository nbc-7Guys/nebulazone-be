package nbc.chillguys.nebulazone.application.pointhistory.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointResponse;
import nbc.chillguys.nebulazone.application.pointhistory.service.PointHistoryService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointHistoryController {

	private final PointHistoryService pointHistoryService;

	@PostMapping("/deposit")
	public ResponseEntity<PointResponse> createPointHistory(
		@RequestBody @Valid PointRequest request,
		@AuthenticationPrincipal AuthUser authUser
	) {
		PointResponse response = pointHistoryService.createPointHistory(request, authUser.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
