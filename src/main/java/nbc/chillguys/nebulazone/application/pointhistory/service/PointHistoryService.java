package nbc.chillguys.nebulazone.application.pointhistory.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class PointHistoryService {

	private final PointHistoryDomainService pointHistoryDomainService;

	public PointResponse createPointHistory(PointRequest request, User user) {

		PointHistoryCommand command = PointHistoryCommand.of(request, user);
		PointHistory pointHistory = pointHistoryDomainService.createPointHistory(command, PointHistoryStatus.PENDING);

		return PointResponse.from(pointHistory);
	}

	@Transactional(readOnly = true)
	public List<PointHistoryResponse> findMyPointRequests(Long userId, PointHistoryStatus status) {
		return pointHistoryDomainService.findPointHistoriesByUserAndStatus(userId, status)
			.stream()
			.map(PointHistoryResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public CommonPageResponse<PointHistoryResponse> findMyPointHistories(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		Page<PointHistory> histories = pointHistoryDomainService.findPointHistoriesByUser(userId, pageable);

		Page<PointHistoryResponse> dtoPage = histories.map(PointHistoryResponse::from);

		return CommonPageResponse.from(dtoPage);
	}

	public void rejectPointRequest(Long userId, Long pointHistoryId) {
		PointHistory pointHistory = pointHistoryDomainService.findActivePointHistory(pointHistoryId);
		pointHistoryDomainService.rejectPointRequest(pointHistory, userId);
	}

}
