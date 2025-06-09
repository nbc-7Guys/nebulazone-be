package nbc.chillguys.nebulazone.domain.pointhistory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.AdminPointHistoryRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryErrorCode;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryException;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;

@Service
@RequiredArgsConstructor
public class AdminPointHistoryDomainService {

	private final PointHistoryRepository pointHistoryRepository;

	public Page<AdminPointHistoryResponse> searchAdminPointHistories(
		AdminPointHistoryRequest request, Pageable pageable) {
		return pointHistoryRepository.searchAdminPointHistories(request, pageable);
	}

	@Transactional
	public void approvePointHistory(Long pointHistoryId) {
		PointHistory pointHistory = findActivePointHistory(pointHistoryId);
		pointHistory.approve();

	}

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
