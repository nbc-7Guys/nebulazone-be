package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.DeletePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.SearchPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostSearchCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;
import nbc.chillguys.nebulazone.domain.post.event.DeletePostEvent;
import nbc.chillguys.nebulazone.domain.post.event.UpdatePostEvent;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostDomainService postDomainService;
	private final GcsClient gcsClient;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public CreatePostResponse createPost(User user, CreatePostRequest request) {
		PostCreateCommand postCreateDto = PostCreateCommand.of(user, request);

		Post createdPost = postDomainService.createPost(postCreateDto);

		postDomainService.savePostToEs(createdPost);

		return CreatePostResponse.from(createdPost);

	}

	@Transactional
	public UpdatePostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
		Post post = postDomainService.findMyActivePost(userId, postId);

		PostUpdateCommand command = request.toCommand();
		Post updatedPost = postDomainService.updatePost(postId, userId, command);

		eventPublisher.publishEvent(new UpdatePostEvent(post));

		return UpdatePostResponse.from(updatedPost);
	}

	public DeletePostResponse deletePost(Long userId, Long postId) {
		postDomainService.deletePost(postId, userId);

		eventPublisher.publishEvent(new DeletePostEvent(postId));

		return DeletePostResponse.from(postId);
	}

	public Page<SearchPostResponse> searchPost(String keyword, PostType type, int page, int size) {
		PostSearchCommand command = PostSearchCommand.of(keyword, type, page, size);

		Page<PostDocument> postDocuments = postDomainService.searchPost(command);

		return postDocuments.map(SearchPostResponse::from);
	}

	public GetPostResponse getPost(Long postId) {
		Post post = postDomainService.getActivePostWithUserAndImages(postId);

		return GetPostResponse.from(post);
	}

	public GetPostResponse updatePostImages(Long postId, List<MultipartFile> imageFiles, User user,
		List<String> remainImageUrls) {

		List<String> postImageUrls = new ArrayList<>(remainImageUrls);
		boolean hasImage = imageFiles != null && !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(gcsClient::uploadFile)
				.toList();
			postImageUrls.addAll(newImageUrls);

		}

		Post post = postDomainService.updatePostImages(postId, postImageUrls, user.getId());

		post.getPostImages().stream()
			.filter(postImage -> !postImageUrls.contains(postImage.getUrl()))
			.forEach((postImage) -> gcsClient.deleteFile(postImage.getUrl()));

		postDomainService.savePostToEs(post);

		return GetPostResponse.from(post);
	}
}
