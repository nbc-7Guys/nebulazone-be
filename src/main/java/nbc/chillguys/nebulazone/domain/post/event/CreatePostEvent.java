package nbc.chillguys.nebulazone.domain.post.event;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public record CreatePostEvent(
	Post post
) {
}
