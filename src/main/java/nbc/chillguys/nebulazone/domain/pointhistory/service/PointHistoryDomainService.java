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

	/**
	 * 포인트 내역을 생성하여 저장합니다.
	 *
	 * @param command 포인트 내역 생성 명령 DTO
	 * @return 생성된 PointHistory 엔티티
	 * @author 정석현
	 */
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

	/**
	 * 특정 유저의 포인트 내역을 상태별로 조회합니다.
	 *
	 * @param userId 유저 ID
	 * @param status 포인트 내역 상태(Null 가능, Null이면 전체 조회)
	 * @return 포인트 내역 리스트
	 * @author 정석현
	 */
	public List<PointHistory> findPointHistoriesByUserAndStatus(Long userId, PointHistoryStatus status) {
		if (status != null) {
			return pointHistoryRepository.findByUserIdAndPointHistoryStatus(userId, status);
		} else {
			return pointHistoryRepository.findByUserId(userId);
		}
	}

	/**
	 * 포인트 환급/충전 요청을 거절 처리합니다.<br>
	 * (상태 검증/예외처리만 담당, 소유자 검증은 Application Service에서 별도 처리)
	 *
	 * @param pointHistoryId 포인트 내역 ID
	 * @throws PointHistoryException 내역이 존재하지 않거나, 상태가 PENDING이 아닐 때
	 * @author 정석현
	 */

	/**
	 * 포인트 환급/충전 요청을 거절 처리합니다.
	 *
	 * @param pointHistory 포인트 히스토리
	 * @throws PointHistoryException 내역이 존재하지 않거나, 상태가 PENDING이 아닐 때
	 * @author 정석현
	 */
	public void rejectPointRequest(PointHistory pointHistory, Long userId) {
		validateOwnership(pointHistory, userId);

		validatePending(pointHistory);

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

	/**
	 * 특정 유저의 포인트 내역을 페이징하여 조회합니다.
	 *
	 * @param userId 유저 ID
	 * @param pageable 페이징 정보
	 * @return 포인트 내역 페이지 객체
	 * @author 정석현
	 */
	public Page<PointHistory> findPointHistoriesByUser(Long userId, Pageable pageable) {
		return pointHistoryRepository.findByUserId(userId, pageable);
	}

	/**
	 * 포인트 내역의 상태가 PENDING인지 검증합니다.
	 *
	 * @param pointHistory 포인트 내역 엔티티
	 * @throws PointHistoryException 상태가 PENDING이 아닐 때
	 * @author 정석현
	 */
	public void validatePending(PointHistory pointHistory) {
		if (pointHistory.getPointHistoryStatus() != PointHistoryStatus.PENDING) {
			throw new PointHistoryException(PointHistoryErrorCode.NOT_PENDING);
		}
	}

	/**
	 * 해당 사용자가 맞는지 검증합니다.
	 *
	 * @param pointHistory 포인트 내역 엔티티
	 * @param userId 유저 아이디
	 * @author 정석현
	 */
	public void validateOwnership(PointHistory pointHistory, Long userId) {
		if (pointHistory.getUser().getId().equals(userId)) {
			return;
		}
		throw new PointHistoryException(PointHistoryErrorCode.NOT_OWNER);
	}

}
