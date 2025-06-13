package nbc.chillguys.nebulazone.domain.post.entity;

import java.util.Arrays;

import lombok.Getter;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;

@Getter
public enum PostType {
	FREE, INFO, QUESTION;

	public static PostType of(String postType) {
		return Arrays.stream(PostType.values())
			.filter(type -> type.name().equalsIgnoreCase(postType))
			.findFirst()
			.orElseThrow(() -> new PostException(PostErrorCode.INVALID_POST_TYPE));
	}
}
