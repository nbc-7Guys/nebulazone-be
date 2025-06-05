package nbc.chillguys.nebulazone.domain.pointhistory.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryErrorCode;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryException;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;

@Service
@RequiredArgsConstructor
public class PointHistoryDomainService {

	private final PointHistoryRepository pointHistoryRepository;

	public PointHistory createPointHistory(PointHistoryCommand command) {
		PointHistory pointHistory = PointHistory.builder()
			.user(command.user())
			.price(command.price())
			.account(command.account())
			.pointHistoryType(command.type())
			.pointHistoryStatus(PointHistoryStatus.PENDING)
			.build();
		return pointHistoryRepository.save(pointHistory);
	}

	public List<PointHistory> findPointHistoriesByUserAndStatus(Long userId, PointHistoryStatus status) {
		if (status != null) {
			return pointHistoryRepository.findByUserIdAndPointHistoryStatus(userId, status);
		} else {
			return pointHistoryRepository.findByUserId(userId);
		}
	}

	public Page<PointHistory> findPointHistoriesByUser(Long userId, Pageable pageable) {
		return pointHistoryRepository.findByUserId(userId, pageable);
	}

	public PointHistory findPointHistory(Long pointHistoryId) {
		return pointHistoryRepository.findActivePointHistoryById(pointHistoryId)
			.orElseThrow(() -> new PointHistoryException(PointHistoryErrorCode.POINT_HISTORY_NOT_FOUND));
	}

	public void validatePending(PointHistory pointHistory) {
		if (pointHistory.getPointHistoryStatus() != PointHistoryStatus.PENDING) {
			throw new PointHistoryException(PointHistoryErrorCode.NOT_PENDING);
		}
	}

}
