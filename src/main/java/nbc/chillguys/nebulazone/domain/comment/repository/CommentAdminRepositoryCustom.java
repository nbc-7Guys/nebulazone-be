package nbc.chillguys.nebulazone.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public interface CommentAdminRepositoryCustom {
	Page<Comment> searchComments(CommentAdminSearchQueryCommand command, Pageable pageable);
}
