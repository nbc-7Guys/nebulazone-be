package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.AdminPostSearchRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.AdminPostUpdateTypeRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.AdminPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.DeletePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostInfo;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.service.AdminPostDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Service
@RequiredArgsConstructor
public class AdminPostService {

	private final AdminPostDomainService adminPostDomainService;
	private final S3Service s3Service;

	public CommonPageResponse<AdminPostResponse> findPosts(AdminPostSearchRequest request, Pageable pageable) {
		AdminPostSearchQueryCommand command = new AdminPostSearchQueryCommand(
			request.keyword(),
			request.type(),
			request.includeDeleted()
		);
		Page<AdminPostInfo> infoPage = this.adminPostDomainService.findPosts(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminPostResponse::from));
	}

	public GetPostResponse getAdminPost(Long postId) {
		Post post = adminPostDomainService.getActivePostWithUserAndImages(postId);

		return GetPostResponse.from(post);
	}

	public UpdatePostResponse updateAdminPost(
		Long postId,
		UpdatePostRequest request,
		List<MultipartFile> imageFiles
	) {
		Post post = adminPostDomainService.findMyActivePost(postId);

		List<String> imageUrls = new ArrayList<>(request.remainImageUrls());
		boolean hasImage = !imageFiles.isEmpty();
		if (hasImage) {
			List<String> newImageUrls = imageFiles.stream()
				.map(s3Service::generateUploadUrlAndUploadFile)
				.toList();
			imageUrls.addAll(newImageUrls);

			post.getPostImages().stream()
				.filter(postImage -> !imageUrls.contains(postImage.getUrl()))
				.forEach((postImage) -> s3Service.generateDeleteUrlAndDeleteFile(postImage.getUrl()));
		}

		AdminPostUpdateCommand command = request.toAdminCommand(postId, imageUrls);

		Post updatedPost = adminPostDomainService.updatePost(command);

		adminPostDomainService.savePostToEs(updatedPost);

		return UpdatePostResponse.from(updatedPost);
	}

	public void updatePostType(Long postId, AdminPostUpdateTypeRequest request) {
		adminPostDomainService.updatePostType(postId, request.type());
	}

	public DeletePostResponse deleteAdminPost(Long postId) {
		adminPostDomainService.deletePost(postId);

		adminPostDomainService.deletePostFromEs(postId);

		return DeletePostResponse.from(postId);
	}

	public void restorePost(Long postId) {
		adminPostDomainService.restorePost(postId);
	}

}
