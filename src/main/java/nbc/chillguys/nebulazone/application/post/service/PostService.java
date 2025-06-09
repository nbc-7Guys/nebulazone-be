package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.DeletePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostDeleteCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Service
@RequiredArgsConstructor
public class PostService {

	private final UserDomainService userDomainService;
	private final PostDomainService postDomainService;
	private final S3Service s3Service;

	public CreatePostResponse createPost(AuthUser authUser, CreatePostRequest request,
		List<MultipartFile> multipartFiles) {

		User findUser = userDomainService.findActiveUserById(authUser.getId());
		PostCreateCommand postCreateDto = PostCreateCommand.of(findUser, request);

		List<String> postImageUrls = multipartFiles == null
			? List.of()
			: multipartFiles.stream()
			.map(s3Service::generateUploadUrlAndUploadFile)
			.toList();

		Post createPost = postDomainService.createPost(postCreateDto, postImageUrls);

		return CreatePostResponse.from(createPost, postImageUrls);

	}

	public UpdatePostResponse updatePost(
		Long userId,
		Long postId,
		UpdatePostRequest request,
		List<MultipartFile> imageFiles
	) {
		Post post = postDomainService.findMyActivePost(userId, postId);

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

		PostUpdateCommand command = request.toCommand(userId, postId, imageUrls);

		Post updatedPost = postDomainService.updatePost(command);

		return UpdatePostResponse.from(updatedPost);
	}

	public DeletePostResponse deletePost(Long userId, Long postId) {
		PostDeleteCommand command = PostDeleteCommand.of(userId, postId);

		postDomainService.deletePost(command);

		return DeletePostResponse.from(postId);
	}
}
