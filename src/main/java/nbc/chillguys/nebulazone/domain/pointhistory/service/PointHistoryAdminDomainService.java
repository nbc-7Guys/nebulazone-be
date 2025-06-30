package nbc.chillguys.nebulazone.domain.pointhistory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryErrorCode;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryException;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;

@Service
@RequiredArgsConstructor
public class PointHistoryAdminDomainService {

	private final PointHistoryRepository pointHistoryRepository;

	/**
	 * 포인트 히스토리 검색
	 *
	 * @param request 검색 조건
	 * @param pageable 페이징
	 * @return 검색 결과
	 * @author 정석현
	 */
	public Page<AdminPointHistoryResponse> searchAdminPointHistories(
		PointHistoryAdminRequest request, Pageable pageable) {
		return pointHistoryRepository.searchAdminPointHistories(request, pageable);
	}

	/**
	 * 포인트 요청 승인
	 *
	 * @param pointHistoryId 포인트 아이디
	 * @return 승인된 포인트 요청
	 * @author 정석현
	 */
	@Transactional
	public PointHistory approvePointHistory(Long pointHistoryId) {
		PointHistory pointHistory = findActivePointHistory(pointHistoryId);
		pointHistory.approve();
		return pointHistory;
	}

	/**
	 * 포인트 요청 거절
	 *
	 * @param pointHistoryId 포인트 아이디
	 * @author 정석현
	 */
	@Transactional
	public void rejectPointHistory(Long pointHistoryId) {
		PointHistory pointHistory = findActivePointHistory(pointHistoryId);

		pointHistory.reject();

	}

	/**
	 * 활성화된(삭제되지 않은) 포인트 내역을 ID로 조회합니다.
	 *
	 * @param pointHistoryId 포인트 내역 ID
	 * @return 활성화된 PointHistory 엔티티
	 * @throws PointHistoryException 내역이 존재하지 않을 때
	 * @author 정석현
	 */
	public PointHistory findActivePointHistory(Long pointHistoryId) {
		return pointHistoryRepository.findActivePointHistoryById(pointHistoryId)
			.orElseThrow(() -> new PointHistoryException(PointHistoryErrorCode.POINT_HISTORY_NOT_FOUND));
	}
}
