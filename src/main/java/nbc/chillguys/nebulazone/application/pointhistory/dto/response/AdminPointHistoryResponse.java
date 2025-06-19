package nbc.chillguys.nebulazone.application.pointhistory.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;

public record AdminPointHistoryResponse(
	Long pointId,
	Long price,
	String account,
	PointHistoryType type,
	PointHistoryStatus status,
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime createdAt,
	Long userId,
	String email,
	String nickname
) {
	@QueryProjection
	public AdminPointHistoryResponse(
		Long pointId, Long price, String account,
		PointHistoryType type, PointHistoryStatus status, LocalDateTime createdAt,
		Long userId, String email, String nickname
	) {
		this.pointId = pointId;
		this.price = price;
		this.account = account;
		this.type = type;
		this.status = status;
		this.createdAt = createdAt;
		this.userId = userId;
		this.email = email;
		this.nickname = nickname;
	}
}
