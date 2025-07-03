package nbc.chillguys.nebulazone.application.pointhistory.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryAdminDomainService;
import nbc.chillguys.nebulazone.domain.user.dto.UserPointChargeCommand;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@Service
@RequiredArgsConstructor
public class PointHistoryAdminService {
	private final PointHistoryAdminDomainService pointHistoryAdminDomainService;
	private final UserDomainService userDomainService;
	private final UserCacheService userCacheService;

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
		PointHistoryAdminRequest request, Pageable pageable) {

		return CommonPageResponse.from(
			pointHistoryAdminDomainService.searchAdminPointHistories(request, pageable)
		);
	}

	/**
	 * 포인트 히스토리 요청을 승인 처리합니다.
	 * 포인트 충전 요청인 경우 유저의 포인트를 충전합니다.
	 *
	 * @param pointHistoryId 포인트 히스토리 ID
	 * @author 정석현
	 */
	@Transactional
	public void approvePointHistory(Long pointHistoryId) {
		PointHistory pointHistory = pointHistoryAdminDomainService.approvePointHistory(pointHistoryId);

		UserPointChargeCommand command = new UserPointChargeCommand(
			pointHistory.getUser().getId(),
			pointHistory.getPrice()
		);

		if (pointHistory.getPointHistoryType() == PointHistoryType.CHARGE) {
			userDomainService.chargeUserPoint(command);
		} else if (pointHistory.getPointHistoryType() == PointHistoryType.EXCHANGE) {
			userDomainService.exchangeUserPoint(command);
		}
		userCacheService.deleteUserById(pointHistory.getUser().getId());
	}

	/**
	 * 포인트 히스토리 요청을 거절 처리합니다.
	 *
	 * @param pointHistoryId 포인트 히스토리 ID
	 * @author 정석현
	 */
	public void rejectPointHistory(Long pointHistoryId) {
		pointHistoryAdminDomainService.rejectPointHistory(pointHistoryId);
	}
}
