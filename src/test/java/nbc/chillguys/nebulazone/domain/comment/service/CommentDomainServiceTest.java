package nbc.chillguys.nebulazone.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.comment.dto.CommentCreateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentDeleteCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentListFindQuery;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentUpdateCommand;
import nbc.chillguys.nebulazone.domain.comment.dto.CommentWithUserInfo;
import nbc.chillguys.nebulazone.domain.comment.entity.Comment;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentErrorCode;
import nbc.chillguys.nebulazone.domain.comment.exception.CommentException;
import nbc.chillguys.nebulazone.domain.comment.repository.CommentRepository;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("커뮤니티 댓글 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CommentDomainServiceTest {

	@InjectMocks
	private CommentDomainService commentDomainService;

	@Mock
	private CommentRepository commentRepository;

	private User user;
	private Post post;
	private Comment comment;
	private Comment childComment;

	@BeforeEach
	void init() {
		List<Address> addresses = new ArrayList<>();
		IntStream.range(1, 4)
			.forEach(i -> addresses.add(
				Address.builder()
					.addressNickname("테스트 주소 닉네임" + i)
					.roadAddress("도로명 주소 테스트" + i)
					.detailAddress("상세 주소 테스트" + i)
					.build()
			));

		user = User.builder()
			.email("test@test.com")
			.password("password")
			.phone("01012345678")
			.nickname("테스트닉")
			.profileImage("test.jpg")
			.point(0)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(addresses)
			.build();
		ReflectionTestUtils.setField(user, "id", 1L);

		post = Post.builder()
			.title("테스트 제목1")
			.content("테스트 본문1")
			.type(PostType.FREE)
			.user(user)
			.build();
		ReflectionTestUtils.setField(post, "id", 1L);

		comment = Comment.builder()
			.post(post)
			.user(user)
			.content("댓글")
			.build();
		ReflectionTestUtils.setField(comment, "id", 1L);

		childComment = Comment.builder()
			.post(post)
			.user(user)
			.parent(comment)
			.content("대댓글")
			.build();
		ReflectionTestUtils.setField(childComment, "id", 2L);
	}

	@Nested
	@DisplayName("댓글 생성 테스트")
	class CreateCommentTest {

		@Test
		@DisplayName("댓글 생성 성공")
		void success_createComment() {
			CommentCreateCommand command = new CommentCreateCommand(user, post, comment.getContent(), -1);

			given(commentRepository.save(any(Comment.class))).willReturn(comment);

			Comment comment = commentDomainService.createComment(command);

			assertNull(comment.getParent());
			verify(commentRepository, times(1)).save(any(Comment.class));
		}

		@Test
		@DisplayName("대댓글 생성 성공")
		void success_createChildComment() {
			CommentCreateCommand command
				= new CommentCreateCommand(user, post, childComment.getContent(), comment.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));
			given(commentRepository.save(any(Comment.class))).willReturn(childComment);

			Comment comment = commentDomainService.createChildComment(command);

			assertNotNull(comment.getParent());
			assertEquals(childComment.getParent().getId(), comment.getParent().getId());
			verify(commentRepository, times(1)).save(any(Comment.class));
		}

		@Test
		@DisplayName("대댓글 생성 실패 - 부모 댓글을 찾을 수 없음")
		void fail_createChildComment_parentCommentNotFound() {
			CommentCreateCommand command
				= new CommentCreateCommand(user, post, childComment.getContent(), comment.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.createChildComment(command));
			assertEquals(CommentErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("댓글 수정 테스트")
	class UpdateCommentTest {

		@Test
		@DisplayName("댓글 수정 성공")
		void success_updateComment() {
			CommentUpdateCommand command
				= new CommentUpdateCommand(comment.getId(), user.getId(), post.getId(), "수정된 댓글");

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			Comment result = commentDomainService.updateComment(command);

			assertEquals(command.content(), result.getContent());
		}

		@Test
		@DisplayName("댓글 수정 실패 - 댓글을 찾을 수 없음")
		void fail_updateComment_commentNotFound() {
			CommentUpdateCommand command
				= new CommentUpdateCommand(comment.getId(), user.getId(), post.getId(), "수정된 댓글");

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.updateComment(command));
			assertEquals(CommentErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("댓글 수정 실패 - 게시글에 해당 댓글을 찾을 수 없음")
		void fail_updateComment_notBelongToPost() {
			Post post = Post.builder().build();
			ReflectionTestUtils.setField(post, "id", 3L);
			CommentUpdateCommand command
				= new CommentUpdateCommand(comment.getId(), user.getId(), post.getId(), "수정된 댓글");

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.updateComment(command));
			assertEquals(CommentErrorCode.NOT_BELONG_TO_POST, exception.getErrorCode());
		}

		@Test
		@DisplayName("댓글 수정 실패 - 댓글 주인이 아님")
		void fail_updateComment_notCommentOwner() {
			User user = User.builder().build();
			ReflectionTestUtils.setField(user, "id", 2L);
			CommentUpdateCommand command
				= new CommentUpdateCommand(comment.getId(), user.getId(), post.getId(), "수정된 댓글");

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.updateComment(command));
			assertEquals(CommentErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("댓글 조회 테스트")
	class FindCommentsTest {

		@Test
		@DisplayName("댓글 조회 성공")
		void success_updateComment() {
			CommentListFindQuery query = new CommentListFindQuery(post.getId(), 0, 20);

			Pageable pageable = PageRequest.of(query.page(), query.size());
			List<CommentWithUserInfo> comments = List.of(
				new CommentWithUserInfo(
					comment.getId(),
					comment.getContent(),
					comment.getUser().getNickname(),
					null,
					comment.getCreatedAt(),
					comment.getModifiedAt()
				)
			);

			PageImpl<CommentWithUserInfo> commentPage = new PageImpl<>(comments, pageable, comments.size());

			given(commentRepository.findComments(any(CommentListFindQuery.class))).willReturn(commentPage);

			Page<CommentWithUserInfo> result = commentDomainService.findComments(query);

			assertEquals(query.page(), result.getPageable().getPageNumber());
			assertEquals(comments.size(), result.getNumberOfElements());
		}
	}

	@Nested
	@DisplayName("댓글 삭제 테스트")
	class DeleteCommentTest {

		@Test
		@DisplayName("댓글 삭제 성공")
		void success_deleteComment() {
			CommentDeleteCommand command = new CommentDeleteCommand(comment.getId(), user.getId(), post.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			commentDomainService.deleteComment(command);

			assertTrue(comment.isDeleted());
			assertNotNull(comment.getDeletedAt());
		}

		@Test
		@DisplayName("댓글 삭제 실패 - 댓글을 찾을 수 없음")
		void fail_deleteComment_commentNotFound() {
			CommentDeleteCommand command = new CommentDeleteCommand(comment.getId(), user.getId(), post.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.empty());

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.deleteComment(command));
			assertEquals(CommentErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("댓글 삭제 실패 - 게시글에 해당 댓글을 찾을 수 없음")
		void fail_deleteComment_notBelongToPost() {
			Post post = Post.builder().build();
			ReflectionTestUtils.setField(post, "id", 3L);
			CommentDeleteCommand command = new CommentDeleteCommand(comment.getId(), user.getId(), post.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.deleteComment(command));
			assertEquals(CommentErrorCode.NOT_BELONG_TO_POST, exception.getErrorCode());
		}

		@Test
		@DisplayName("댓글 삭제 실패 - 댓글 주인이 아님")
		void fail_deleteComment_notCommentOwner() {
			User user = User.builder().build();
			ReflectionTestUtils.setField(user, "id", 2L);
			CommentDeleteCommand command = new CommentDeleteCommand(comment.getId(), user.getId(), post.getId());

			given(commentRepository.findByIdAndDeletedFalse(any(Long.class))).willReturn(Optional.ofNullable(comment));

			CommentException exception = assertThrows(CommentException.class,
				() -> commentDomainService.deleteComment(command));
			assertEquals(CommentErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
		}
	}
}
