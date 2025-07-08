package nbc.chillguys.nebulazone.application.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.SearchPostResponse;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostSearchCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.event.CreatePostEvent;
import nbc.chillguys.nebulazone.domain.post.event.UpdatePostEvent;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@DisplayName("게시글 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@Mock
	private PostDomainService postDomainService;

	@Mock
	private GcsClient gcsClient;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private PostService postService;

	private User user;
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

	@Nested
	@DisplayName("게시글 검색 테스트")
	class SearchPostTest {
		@Test
		@DisplayName("게시글 검색 성공")
		void success_searchPost() {
			// Given
			PostSearchCommand command = PostSearchCommand.of("테스트", PostType.FREE, 1, 10);

			given(postDomainService.searchPost(command))
				.willReturn(new PageImpl<>(List.of(PostDocument.from(post)),
					PageRequest.of(0, 10), 1L));

			// When
			Page<SearchPostResponse> responses = postService.searchPost("테스트", PostType.FREE, 1, 10);

			// Then
			assertThat(responses.getContent()).hasSize(1);

			verify(postDomainService, times(1)).searchPost(command);

		}
	}

	@Nested
	@DisplayName("게시글 조회 테스트")
	class GetPostTest {
		@Test
		void success_getPost() {
			// Given
			Long postId = 1L;

			given(postDomainService.getActivePostWithUserAndImages(postId))
				.willReturn(post);

			// When
			GetPostResponse response = postService.getPost(postId);

			// Then
			assertThat(response.postId()).isEqualTo(postId);
			assertThat(response.title()).isEqualTo("테스트 제목1");
			assertThat(response.content()).isEqualTo("테스트 본문1");
			assertThat(response.type()).isEqualTo(PostType.FREE.name());

			verify(postDomainService, times(1)).getActivePostWithUserAndImages(postId);
		}
	}

	@Nested
	@DisplayName("게시글 생성 테스트")
	class CreatePostTest {

		@Test
		@DisplayName("게시글 생성 성공 - 자유게시판")
		void success_createPost() {
			// given
			CreatePostRequest request = new CreatePostRequest(
				"자유게시판 제목",
				"자유게시판 내용",
				"FREE");

			given(postDomainService.createPost(any(PostCreateCommand.class))).willReturn(post);
			willDoNothing().given(eventPublisher).publishEvent(any(CreatePostEvent.class));

			// when
			CreatePostResponse response = postService.createPost(user, request);

			// then
			verify(postDomainService, times(1)).createPost(any(PostCreateCommand.class));
			verify(eventPublisher, times(1)).publishEvent(any(CreatePostEvent.class));

			assertThat(response.postId()).isEqualTo(post.getId());
			assertThat(response.title()).isEqualTo(post.getTitle());
		}
	}

	@Nested
	@DisplayName("게시글 이미지 수정 테스트")
	class UpdatePostImagesTest {

		@DisplayName("게시글 이미지 수정 성공")
		@Test
		void success_updatePostImages() {
			// given
			Long updatePostId = 1L;
			post.updatePostImages(List.of("old_image_url1", "old_image_url2"));

			List<String> remainImageUrls = List.of("old_image_url1");

			List<MultipartFile> newImageFiles = List.of(
				new MockMultipartFile(
					"new_image1",
					"new_image1.jpg",
					"image/jpeg",
					"new_image1_content"
						.getBytes()),

				new MockMultipartFile(
					"new_image2",
					"new_image2.jpg",
					"image/jpeg",
					"new_image2_content"
						.getBytes()));

			given(gcsClient.uploadFile(any(MultipartFile.class)))
				.willReturn("new_image_url1", "new_image_url2");

			List<String> updatedImageUrls = List.of(
				"old_image_url1",
				"new_image_url1",
				"new_image_url2"
			);

			post.updatePostImages(updatedImageUrls);

			given(postDomainService.findActivePost(updatePostId)).willReturn(post);
			given(postDomainService.updatePostImages(any(Post.class), eq(updatedImageUrls), eq(user.getId())))
				.willReturn(post);
			willDoNothing().given(eventPublisher).publishEvent(any(UpdatePostEvent.class));

			// when
			GetPostResponse result = postService.updatePostImages(updatePostId, newImageFiles, user,
				remainImageUrls);

			// then
			verify(gcsClient, times(2)).uploadFile(any(MultipartFile.class));
			verify(postDomainService, times(1)).findActivePost(updatePostId);
			verify(postDomainService, times(1))
				.updatePostImages(any(Post.class), anyList(), anyLong());
			verify(eventPublisher, times(1)).publishEvent(any(UpdatePostEvent.class));

			assertThat(result.imageUrls())
				.containsExactly("old_image_url1", "new_image_url1", "new_image_url2");
			assertThat(result.imageUrls()).doesNotContain("old_image_url2");
		}
	}

}
