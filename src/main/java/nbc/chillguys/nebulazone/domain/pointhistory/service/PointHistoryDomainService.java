package nbc.chillguys.nebulazone.domain.pointhistory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryErrorCode;
import nbc.chillguys.nebulazone.domain.pointhistory.exception.PointHistoryException;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

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
			return pointHistoryRepository.findByUser_IdAndPointHistoryStatus(userId, status);
		} else {
			return pointHistoryRepository.findByUser_Id(userId);
		}
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

	public void validateEnoughBalance(User user, int amount) {
		if (user.getPoint() < amount) {
			throw new PointHistoryException(PointHistoryErrorCode.INSUFFICIENT_BALANCE);
		}
	}
}
