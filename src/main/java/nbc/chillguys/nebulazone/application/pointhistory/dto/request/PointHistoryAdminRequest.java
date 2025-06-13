package nbc.chillguys.nebulazone.application.pointhistory.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record PointHistoryAdminRequest(
	String email,
	String nickname,
	PointHistoryType type,
	PointHistoryStatus status,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime startDate,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime endDate
) {
}
