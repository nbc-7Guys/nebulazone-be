package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import lombok.Builder;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;

@Builder
public record PointResponse(
	Long id,
	Integer point
) {
	public static PointResponse from(PointHistory pointHistory) {
		return PointResponse.builder()
			.id(pointHistory.getId())
			.point(pointHistory.getPrice())
			.build();
	}
}
