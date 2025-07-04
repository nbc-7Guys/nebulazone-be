package nbc.chillguys.nebulazone.application.post.dto.request;

import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record PostAdminSearchRequest(
	String keyword,
	PostType type,
	Boolean includeDeleted,
	int page,
	int size
) {
}
