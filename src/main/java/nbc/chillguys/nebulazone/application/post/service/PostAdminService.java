package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
import nbc.chillguys.nebulazone.domain.post.service.PostAdminDomainService;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;

@Service
@RequiredArgsConstructor
public class PostAdminService {

	private final PostAdminDomainService postsAdminDomainService;
	private final GcsClient gcsClient;

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

	public UpdatePostResponse updateAdminPost(
		Long postId,
		UpdatePostRequest request,
		List<MultipartFile> imageFiles
	) {
		Post post = postsAdminDomainService.findMyActivePost(postId);

		List<String> imageUrls = new ArrayList<>(request.remainImageUrls());
		boolean hasImage = !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(gcsClient::uploadFile)
				.toList();
			imageUrls.addAll(newImageUrls);

			post.getPostImages().stream()
				.filter(postImage -> !imageUrls.contains(postImage.getUrl()))
				.forEach((postImage) -> gcsClient.deleteFile(postImage.getUrl()));
		}

		PostAdminUpdateCommand command = request.toAdminCommand(postId, imageUrls);

		Post updatedPost = postsAdminDomainService.updatePost(command);

		postsAdminDomainService.savePostToEs(updatedPost);

		return UpdatePostResponse.from(updatedPost);
	}

	public void updatePostType(Long postId, PostAdminUpdateTypeRequest request) {
		postsAdminDomainService.updatePostType(postId, request.type());
	}

	public DeletePostResponse deleteAdminPost(Long postId) {
		postsAdminDomainService.deletePost(postId);

		postsAdminDomainService.deletePostFromEs(postId);

		return DeletePostResponse.from(postId);
	}

	public void restorePost(Long postId) {
		postsAdminDomainService.restorePost(postId);
	}

}
