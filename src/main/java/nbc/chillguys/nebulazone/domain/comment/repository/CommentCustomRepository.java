package nbc.chillguys.nebulazone.domain.comment.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;

public interface CommentCustomRepository {
	Page<CommentWithUserInfo> findComments(Long postId, int page, int size);
}
