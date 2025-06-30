package nbc.chillguys.nebulazone.domain.post.dto;

public record PostUpdateCommand(
	String title,
	String content) {
}
