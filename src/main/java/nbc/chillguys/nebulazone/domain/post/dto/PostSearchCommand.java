package nbc.chillguys.nebulazone.domain.post.dto;

import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record PostSearchCommand(
	String keyword,
	String type,
	int page,
	int size
) {
	public static PostSearchCommand of(String keyword, PostType type, int page, int size) {
		return new PostSearchCommand(
			keyword,
			type.name(),
			page,
			size
		);
	}
}
