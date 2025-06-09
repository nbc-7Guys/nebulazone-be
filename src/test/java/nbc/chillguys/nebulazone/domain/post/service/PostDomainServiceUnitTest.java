package nbc.chillguys.nebulazone.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
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
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostDeleteCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("커뮤니티 게시글 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostDomainServiceUnitTest {

	@Mock
	PostRepository postRepository;

	@InjectMocks
	private PostDomainService postDomainService;

	private Post post;

	@BeforeEach
	void init() {
		HashSet<Address> addresses = new HashSet<>();

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
			// given
			PostCreateCommand postCreateCommand = PostCreateCommand.of(user,
				new CreatePostRequest("테스트 제목1", "테스트 본문1", "free"));

			List<String> imageUrls = List.of("image1.jpg, image2.jpg");

			Post savedPost = Post.builder()
				.title("테스트 제목1")
				.content("테스트 본문1")
				.type(PostType.FREE)
				.user(user)
				.build();
			ReflectionTestUtils.setField(savedPost, "id", 1L);

			given(postRepository.save(any(Post.class))).will(i -> {
				Post post = i.getArgument(0);
				post.addPostImages(imageUrls);
				return post;
			});

			// when
			Post result = postDomainService.createPost(postCreateCommand, imageUrls);

			// then
			assertThat(result.getTitle()).isEqualTo(postCreateCommand.title());
			assertThat(result.getContent()).isEqualTo(postCreateCommand.content());
			assertThat(result.getType()).isEqualTo(PostType.FREE);
			assertThat(result.getPostImages().size()).isEqualTo(2);
			assertThat(result.getUser().getNickname()).isEqualTo(user.getNickname());
			assertThat(result.getUser().getAddresses().size()).isEqualTo(3);

			verify(postRepository, times(1)).save(any(Post.class));
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
				= new PostUpdateCommand(user.getId(), post.getId(), "수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.ofNullable(post));

			Post result = postDomainService.updatePost(command);

			assertEquals(command.title(), result.getTitle());
			assertEquals(command.content(), result.getContent());
			assertEquals(command.imageUrls().size(), result.getPostImages().size());
		}

		@Test
		@DisplayName("게시글 수정 실패 - 게시글을 찾을 수 없음")
		void fail_updatePost_postNotFound() {
			List<String> imageUrls = List.of("image1.jpg, image2.jpg");
			PostUpdateCommand command
				= new PostUpdateCommand(user.getId(), post.getId(), "수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.empty());

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.updatePost(command));

			assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("게시글 수정 실패 - 게시글 주인이 아님")
		void fail_updatePost_notPostOwner() {
			List<String> imageUrls = List.of("image1.jpg, image2.jpg");
			PostUpdateCommand command
				= new PostUpdateCommand(2L, post.getId(), "수정된 제목", "수정된 본문", imageUrls);

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.ofNullable(post));

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.updatePost(command));

			assertEquals(PostErrorCode.NOT_POST_OWNER, exception.getErrorCode());
		}
	}

	@Nested
	@DisplayName("게시글 삭제 테스트")
	class DeletePostTest {

		@Test
		@DisplayName("게시글 삭제 성공")
		void success_deletePost() {
			PostDeleteCommand command = new PostDeleteCommand(user.getId(), post.getId());

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.ofNullable(post));

			postDomainService.deletePost(command);

			assertTrue(post.isDeleted());
			assertNotNull(post.getDeletedAt());
		}

		@Test
		@DisplayName("게시글 삭제 실패 - 게시글을 찾을 수 없음")
		void fail_deletePost_postNotFound() {
			PostDeleteCommand command = new PostDeleteCommand(user.getId(), post.getId());

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.empty());

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.deletePost(command));

			assertEquals(PostErrorCode.POST_NOT_FOUND, exception.getErrorCode());
		}

		@Test
		@DisplayName("게시글 삭제 실패 - 게시글 주인이 아님")
		void fail_deletePost_notPostOwner() {
			PostDeleteCommand command = new PostDeleteCommand(2L, post.getId());

			given(postRepository.findActivePostById(post.getId())).willReturn(Optional.ofNullable(post));

			PostException exception
				= assertThrows(PostException.class, () -> postDomainService.deletePost(command));

			assertEquals(PostErrorCode.NOT_POST_OWNER, exception.getErrorCode());
		}
	}

}
