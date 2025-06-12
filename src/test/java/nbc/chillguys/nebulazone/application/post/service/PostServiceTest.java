package nbc.chillguys.nebulazone.application.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.SearchPostResponse;
import nbc.chillguys.nebulazone.domain.post.dto.PostSearchCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@DisplayName("게시글 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {
	@Mock
	private UserDomainService userDomainService;

	@Mock
	private PostDomainService postDomainService;

	@Mock
	private S3Service s3Service;

	@InjectMocks
	private PostService postService;

	private User user;

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
}
