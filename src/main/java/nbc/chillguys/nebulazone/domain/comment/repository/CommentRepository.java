package nbc.chillguys.nebulazone.domain.comment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.comment.entity.Comment;

public interface CommentRepository
	extends JpaRepository<Comment, Long>, CommentCustomRepository, CustomCommentAdminRepository {

	Optional<Comment> findByIdAndDeletedFalse(Long commentId);
}
