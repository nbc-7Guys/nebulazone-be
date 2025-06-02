package nbc.chillguys.nebulazone.domain.post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.exception.PostErrorCode;
import nbc.chillguys.nebulazone.domain.post.exception.PostException;
import nbc.chillguys.nebulazone.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDomainService {

	private final PostRepository postRepository;

	@Transactional
	public Post createPost(PostCreateCommand command, List<String> postImageUrls) {

		Post post = Post.of(command.title(), command.content(), command.type(), command.user());
		Post savePost = postRepository.save(post);
		savePost.addPostImages(postImageUrls);

		return savePost;
	}

	@Transactional
	public Post updatePost(PostUpdateCommand command) {
		Post post = findActivePost(command.postId());

		post.update(command.title(), command.content(), command.imageUrls());

		return post;
	}

	public Post findActivePost(Long postId) {
		return postRepository.findById(postId)
			.orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
	}
}
