package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

@Builder
public record PointHistoryResponse(
	Integer price,
	String account,
	PointHistoryType type,
	PointHistoryStatus status,
	LocalDateTime createdAt
) {
	public static PointHistoryResponse from(PointHistory pointHistory) {
		return PointHistoryResponse.builder()
			.price(pointHistory.getPrice())
			.account(pointHistory.getAccount())
			.type(pointHistory.getPointHistoryType())
			.status(pointHistory.getPointHistoryStatus())
			.createdAt(pointHistory.getCreatedAt())
			.build();
	}
}
