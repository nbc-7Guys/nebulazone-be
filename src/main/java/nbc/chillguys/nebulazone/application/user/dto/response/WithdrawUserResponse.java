package nbc.chillguys.nebulazone.application.user.dto.response;

public record WithdrawUserResponse(
	Long userId
) {
	public static WithdrawUserResponse from(Long userId) {
		return new WithdrawUserResponse(userId);
	}
}
