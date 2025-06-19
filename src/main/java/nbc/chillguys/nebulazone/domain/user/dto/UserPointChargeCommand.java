package nbc.chillguys.nebulazone.domain.user.dto;

public record UserPointChargeCommand(
	Long userId,
	Long point
) {
}
