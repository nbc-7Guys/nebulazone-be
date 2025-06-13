package nbc.chillguys.nebulazone.application.post.dto.request;

import nbc.chillguys.nebulazone.domain.post.entity.PostType;

public record PostAdminUpdateTypeRequest(
	PostType type
) {

}
