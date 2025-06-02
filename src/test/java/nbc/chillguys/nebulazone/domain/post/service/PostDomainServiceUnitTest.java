package nbc.chillguys.nebulazone.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@ExtendWith(MockitoExtension.class)
class PostDomainServiceUnitTest {

	@Mock
	PostRepository postRepository;

	@InjectMocks
	private PostDomainService postDomainService;

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
			.oauthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(addresses)
			.build();

		ReflectionTestUtils.setField(user, "id", 1L);
	}

	private User user;

	@Test
	void create_post_success() {
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
