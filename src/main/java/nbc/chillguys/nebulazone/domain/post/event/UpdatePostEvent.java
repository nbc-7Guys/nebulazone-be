package nbc.chillguys.nebulazone.domain.post.event;

import nbc.chillguys.nebulazone.domain.post.entity.Post;

public record UpdatePostEvent(
	Post post
) {
}
