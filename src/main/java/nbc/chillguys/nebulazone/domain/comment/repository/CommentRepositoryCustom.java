package nbc.chillguys.nebulazone.domain.comment.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentListFindQuery;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;

public interface CommentRepositoryCustom {
	Page<CommentWithUserInfo> findComments(CommentListFindQuery query);
}
