package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

@Builder
public record PointResponse(
	Long id,
	Integer price,
	PointHistoryType type,
	PointHistoryStatus status,
	LocalDateTime createdAt
) {
	public static PointResponse from(PointHistory pointHistory) {
		return PointResponse.builder()
			.id(pointHistory.getId())
			.price(pointHistory.getPrice())
			.status(pointHistory.getPointHistoryStatus())
			.type(pointHistory.getPointHistoryType())
			.createdAt(pointHistory.getCreatedAt())
			.build();
	}
}
