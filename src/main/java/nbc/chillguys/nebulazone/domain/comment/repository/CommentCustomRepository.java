package nbc.chillguys.nebulazone.domain.comment.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserDto;

public interface CommentCustomRepository {
	Page<CommentWithUserDto> findComments(Long postId, int page, int size);
}
