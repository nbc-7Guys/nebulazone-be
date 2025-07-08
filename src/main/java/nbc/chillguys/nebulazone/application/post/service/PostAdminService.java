package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.PostAdminSearchRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.PostAdminUpdateTypeRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.DeletePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.PostAdminResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminInfo;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.event.CreatePostEvent;
import nbc.chillguys.nebulazone.domain.post.event.DeletePostEvent;
import nbc.chillguys.nebulazone.domain.post.event.UpdatePostEvent;
import nbc.chillguys.nebulazone.domain.post.service.PostAdminDomainService;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@Service
@RequiredArgsConstructor
public class PostAdminService {

	private final PostAdminDomainService postsAdminDomainService;
	private final GcsClient gcsClient;
	private final ApplicationEventPublisher eventPublisher;

	public CommonPageResponse<PostAdminResponse> findPosts(PostAdminSearchRequest request, Pageable pageable) {
		PostAdminSearchQueryCommand command = new PostAdminSearchQueryCommand(
			request.keyword(),
			request.type(),
			request.includeDeleted()
		);
		Page<PostAdminInfo> infoPage = this.postsAdminDomainService.findPosts(command, pageable);
		return CommonPageResponse.from(infoPage.map(PostAdminResponse::from));
	}

	public GetPostResponse getAdminPost(Long postId) {
		Post post = postsAdminDomainService.getActivePostWithUserAndImages(postId);

		return GetPostResponse.from(post);
	}

	public UpdatePostResponse updateAdminPost(Long postId, UpdatePostRequest request) {
		PostAdminUpdateCommand command = request.toAdminCommand(postId);

		Post updatedPost = postsAdminDomainService.updatePost(command);

		eventPublisher.publishEvent(new UpdatePostEvent(updatedPost));

		return UpdatePostResponse.from(updatedPost);
	}

	public void updatePostType(Long postId, PostAdminUpdateTypeRequest request) {
		Post post = postsAdminDomainService.updatePostType(postId, request.type());
		eventPublisher.publishEvent(new UpdatePostEvent(post));

	}

	public DeletePostResponse deleteAdminPost(Long postId) {
		Long deletedPostId = postsAdminDomainService.deletePost(postId);

		eventPublisher.publishEvent(new DeletePostEvent(deletedPostId));

		return DeletePostResponse.from(postId);
	}

	public void restorePost(Long postId) {
		Post post = postsAdminDomainService.restorePost(postId);
		eventPublisher.publishEvent(new CreatePostEvent(post));

	}

	@Transactional
	public GetPostResponse updatePostImages(Long postId, List<MultipartFile> imageFiles,
		List<String> remainImageUrls) {

		List<String> postImageUrls = new ArrayList<>(remainImageUrls);

		boolean hasImage = imageFiles != null && !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(gcsClient::uploadFile)
				.toList();
			postImageUrls.addAll(newImageUrls);
		}

		Post post = postsAdminDomainService.findActivePost(postId);

		post.getPostImages().stream()
			.filter(postImage -> !postImageUrls.contains(postImage.getUrl()))
			.forEach((postImage) -> gcsClient.deleteFile(postImage.getUrl()));

		Post updatedPost = postsAdminDomainService.updatePostImages(post, postImageUrls);

		eventPublisher.publishEvent(new UpdatePostEvent(updatedPost));

		return GetPostResponse.from(updatedPost);
	}
}
