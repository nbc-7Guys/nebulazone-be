package nbc.chillguys.nebulazone.domain.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentInfo;
import nbc.chillguys.nebulazone.domain.comment.dto.AdminCommentSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class AdminCommentDomainService {
	private final CommentRepository commentRepository;

	public Page<AdminCommentInfo> findComments(AdminCommentSearchQueryCommand command, Pageable pageable) {
		return commentRepository.searchComments(command, pageable)
			.map(AdminCommentInfo::from);
	}
}
