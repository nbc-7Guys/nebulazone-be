package nbc.chillguys.nebulazone.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.post.dto.PostSearchCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostEsRepository;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("커뮤니티 게시글 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostDomainServiceUnitTest {

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostEsRepository postEsRepository;

	@InjectMocks
	private PostDomainService postDomainService;

	private Post post;

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
	}

	private User user;

	@Nested
	@DisplayName("게시글 생성 테스트")
	class CreatePostTest {

		@Test
		@DisplayName("게시글 생성 성공")
		void success_createPost() {
			// 다시 짜야함
		}
	}

	@Nested
	@DisplayName("게시글 수정 테스트")
	class UpdatePostTest {

		@Test
		@DisplayName("게시글 수정 성공")
		void success_updatePost() {
			List<String> imageUrls = List.of("image1.jpg, image2.jpg");
			PostUpdateCommand command
				= new PostUpdateCommand("수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.ofNullable(post));

			Post result = postDomainService.updatePost(post.getId(), user.getId(), command);

			assertEquals(command.title(), result.getTitle());
			assertEquals(command.content(), result.getContent());
			assertEquals(command.imageUrls().size(), result.getPostImages().size());
		}

		@Test
		@DisplayName("게시글 수정 실패 - 게시글을 찾을 수 없음")
		void fail_updatePost_postNotFound() {
			List<String> imageUrls = List.of("image1.jpg, image2.jpg");
			PostUpdateCommand command
				= new PostUpdateCommand("수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.empty());

			PostException exception
				= assertThrows(PostException.class,
				() -> postDomainService.updatePost(post.getId(), user.getId(), command));

			assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("게시글 수정 실패 - 게시글 주인이 아님")
		void fail_updatePost_notPostOwner() {
			List<String> imageUrls = List.of("image1.jpg, image2.jpg");
			PostUpdateCommand command
				= new PostUpdateCommand("수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.ofNullable(post));

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.updatePost(post.getId(), 2L, command));

			assertEquals(PostErrorCode.NOT_POST_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("게시글 삭제 테스트")
	class DeletePostTest {

		@Test
		@DisplayName("게시글 삭제 성공")
		void success_deletePost() {
			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.ofNullable(post));

			postDomainService.deletePost(post.getId(), user.getId());

			assertTrue(post.isDeleted());
			assertNotNull(post.getDeletedAt());
		}

		@Test
		@DisplayName("게시글 삭제 실패 - 게시글을 찾을 수 없음")
		void fail_deletePost_postNotFound() {
			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.empty());

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.deletePost(post.getId(), user.getId()));

			assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("게시글 삭제 실패 - 게시글 주인이 아님")
		void fail_deletePost_notPostOwner() {
			given(postRepository.findActivePostByIdWithUser(post.getId())).willReturn(Optional.ofNullable(post));

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.deletePost(post.getId(), 2L));

			assertEquals(PostErrorCode.NOT_POST_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("ES에 게시글 저장 테스트")
	class SavePostToEsTest {
		@Test
		@DisplayName("ES에 게시글 저장 성공")
		void success_savePostToEs() {
			// Given
			PostDocument expectedDoc = PostDocument.from(post);

			// When
			postDomainService.savePostToEs(post);

			// Then
			ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);
			verify(postEsRepository).save(captor.capture());

			PostDocument actualDoc = captor.getValue();
			assertThat(actualDoc.postId()).isEqualTo(expectedDoc.postId());
			assertThat(actualDoc.title()).isEqualTo(expectedDoc.title());
			assertThat(actualDoc.content()).isEqualTo(expectedDoc.content());
			verifyNoMoreInteractions(postEsRepository);

		}
	}

	@Nested
	@DisplayName("ES 게시글 삭제 테스트")
	class DeleteProductFromEsTest {
		@Test
		@DisplayName("ES에 게시글 삭제 성공")
		void success_deletePostFromEs() {
			// Given
			Long postId = 1L;

			// When
			postDomainService.deletePostFromEs(postId);

			// Then
			verify(postEsRepository, times(1)).deleteById(postId);
			verifyNoMoreInteractions(postEsRepository);

		}
	}

	@Nested
	@DisplayName("게시글 검색 테스트")
	class SearchPostTest {
		@Test
		@DisplayName("게시글 검색 성공 - 모든 조건")
		void success_searchPost_allParameters() {
			// Given
			PostSearchCommand command = PostSearchCommand.of("테스트", PostType.FREE, 1, 10);

			given(postEsRepository.searchPost(anyString(), anyString(), any()))
				.willReturn(new PageImpl<>(List.of(PostDocument.from(post)),
					PageRequest.of(0, 10), 1L));

			// When
			Page<PostDocument> postDocuments = postDomainService.searchPost(command);

			// Then
			verify(postEsRepository, times(1))
				.searchPost("테스트", PostType.FREE.name(), PageRequest.of(0, 10));
			assertThat(postDocuments.getContent().size())
				.isEqualTo(1);
			assertThat(postDocuments.getTotalElements())
				.isEqualTo(1);
			assertThat(postDocuments.getContent().getFirst().title())
				.isEqualTo(post.getTitle());
			assertThat(postDocuments.getContent().getFirst().content())
				.isEqualTo(post.getContent());

		}

		@Test
		@DisplayName("게시글 검색 성공 - 게시글 유형만 검색")
		void success_searchPost_noParameters() {
			// Given
			PostSearchCommand command = PostSearchCommand.of(null, PostType.FREE, 1, 10);

			given(postEsRepository.searchPost(any(), anyString(), any()))
				.willReturn(new PageImpl<>(List.of(PostDocument.from(post), PostDocument.from(post)),
					PageRequest.of(0, 10), 2L));

			// When
			Page<PostDocument> postDocuments = postDomainService.searchPost(command);

			// Then
			verify(postEsRepository, times(1))
				.searchPost(null, PostType.FREE.name(), PageRequest.of(0, 10));
			assertThat(postDocuments.getContent().size())
				.isEqualTo(2);
			assertThat(postDocuments.getTotalElements())
				.isEqualTo(2);

		}
	}

	@Nested
	@DisplayName("게시글 조회 테스트")
	class GetPostTest {
		@Test
		@DisplayName("게시글 조회 성공")
		void success_getActivePostWithUserAndImages() {
			// Given
			Long postId = 1L;

			Post mockPost = mock(Post.class);
			given(postRepository.findActivePostByIdWithUserAndImages(anyLong()))
				.willReturn(Optional.of(mockPost));

			// When
			Post result = postDomainService.getActivePostWithUserAndImages(postId);

			// Then
			assertEquals(mockPost, result);

			verify(postRepository, times(1)).findActivePostByIdWithUserAndImages(postId);
		}

		@Test
		@DisplayName("게시글 조회 실패 - 게시글이 존재 하지 않음")
		void fail_getActivePostWithUserAndImages_postNotFound() {
			// Given
			Long postId = 2L;

			given(postRepository.findActivePostByIdWithUserAndImages(anyLong()))
				.willReturn(Optional.empty());

			// When
			PostException exception = assertThrows(PostException.class,
				() -> postDomainService.getActivePostWithUserAndImages(postId));

			// Then
			assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());

			verify(postRepository, times(1)).findActivePostByIdWithUserAndImages(postId);
			verifyNoMoreInteractions(postRepository);
		}
	}

}
