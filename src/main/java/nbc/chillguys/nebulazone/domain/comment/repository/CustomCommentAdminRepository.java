package nbc.chillguys.nebulazone.domain.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public interface CustomCommentAdminRepository {
	Page<Comment> searchComments(AdminCommentSearchQueryCommand command, Pageable pageable);
}
