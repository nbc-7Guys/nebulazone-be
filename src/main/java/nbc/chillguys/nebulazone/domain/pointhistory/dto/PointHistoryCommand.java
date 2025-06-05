package nbc.chillguys.nebulazone.domain.pointhistory.dto;

import lombok.Builder;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Builder
public record PointHistoryCommand(
	User user,
	Integer price,
	String account,
	PointHistoryType type
) {
	public static PointHistoryCommand of(PointRequest request, User user) {
		return PointHistoryCommand.builder()
			.user(user)
			.price(request.price())
			.account(request.account())
			.type(request.type())
			.build();
	}
}
