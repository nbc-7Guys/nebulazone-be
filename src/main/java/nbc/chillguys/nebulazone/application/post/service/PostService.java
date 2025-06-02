package nbc.chillguys.nebulazone.application.post.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
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

		List<String> productImageUrls = multipartFiles.stream()
			.map(s3Service::generateUploadUrlAndUploadFile)
			.toList();

		Post createPost = postDomainService.createPost(postCreateDto, productImageUrls);

		return CreatePostResponse.from(createPost, productImageUrls);

	}

}
