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

	/**
	 * 포인트 히스토리 목록을 검색/조회합니다.<br>
	 * 조건/페이징/정렬을 지원하며, CommonPageResponse 형태로 반환합니다.
	 *
	 * @param request 어드민 포인트 히스토리 검색 조건 DTO
	 * @param pageable 페이징 및 정렬 정보
	 * @return 페이징된 포인트 히스토리 리스트
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public CommonPageResponse<AdminPointHistoryResponse> searchAdminPointHistories(
		AdminPointHistoryRequest request, Pageable pageable) {

		return CommonPageResponse.from(
			adminPointHistoryDomainService.searchAdminPointHistories(request, pageable)
		);
	}

	/**
	 * 포인트 히스토리 요청을 승인 처리합니다.
	 *
	 * @param pointHistoryId 포인트 히스토리 ID
	 * @author 정석현
	 */
	public void approvePointHistory(Long pointHistoryId) {
		adminPointHistoryDomainService.approvePointHistory(pointHistoryId);
	}

	/**
	 * 포인트 히스토리 요청을 거절 처리합니다.
	 *
	 * @param pointHistoryId 포인트 히스토리 ID
	 * @author 정석현
	 */
	public void rejectPointHistory(Long pointHistoryId) {
		adminPointHistoryDomainService.rejectPointHistory(pointHistoryId);
	}
}
