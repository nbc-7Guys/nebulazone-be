package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record PointHistoryResponse(
	Integer price,
	String account,
	PointHistoryType type,
	PointHistoryStatus status,
	LocalDateTime createdAt
) {
	public static PointHistoryResponse from(PointHistory pointHistory) {
		return new PointHistoryResponse(
			pointHistory.getPrice(),
			pointHistory.getAccount(),
			pointHistory.getPointHistoryType(),
			pointHistory.getPointHistoryStatus(),
			pointHistory.getCreatedAt()
		);
	}
}
