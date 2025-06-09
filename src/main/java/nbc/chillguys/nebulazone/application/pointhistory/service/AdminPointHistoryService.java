package nbc.chillguys.nebulazone.application.pointhistory.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.AdminPointHistoryRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.service.AdminPointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;

@Service
@RequiredArgsConstructor
public class AdminPointHistoryService {
	private final PointHistoryDomainService pointHistoryDomainService;
	private final AdminPointHistoryDomainService adminPointHistoryDomainService;

	@Transactional(readOnly = true)
	public CommonPageResponse<AdminPointHistoryResponse> searchAdminPointHistories(
		AdminPointHistoryRequest request, Pageable pageable) {

		return CommonPageResponse.from(
			adminPointHistoryDomainService.searchAdminPointHistories(request, pageable)
		);
	}

	public void approvePointHistory(Long pointHistoryId) {
		adminPointHistoryDomainService.approvePointHistory(pointHistoryId);
	}

	public void rejectPointHistory(Long pointHistoryId) {
		adminPointHistoryDomainService.rejectPointHistory(pointHistoryId);
	}
}
