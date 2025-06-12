package nbc.chillguys.nebulazone.application.pointhistory.dto.request;

import java.time.LocalDateTime;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record AdminPointHistoryRequest(
	String email,
	String nickname,
	PointHistoryType type,
	PointHistoryStatus status,
	LocalDateTime startDate,
	LocalDateTime endDate
) {
}
