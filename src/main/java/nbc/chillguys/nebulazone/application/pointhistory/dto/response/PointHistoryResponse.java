package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

@Builder
public record PointHistoryResponse(
	Long id,
	int point,
	PointHistoryType type,
	PointHistoryStatus status,
	LocalDateTime createdAt
) {
	public static PointHistoryResponse from(Long id, int point, PointHistoryType type, PointHistoryStatus status,
		LocalDateTime createdAt) {
		return PointHistoryResponse.builder()
			.id(id)
			.point(point)
			.type(type)
			.status(status)
			.createdAt(createdAt)
			.build();
	}
}
