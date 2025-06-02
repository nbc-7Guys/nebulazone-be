package nbc.chillguys.nebulazone.application.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.post.dto.PostCreateCommand;
import nbc.chillguys.nebulazone.domain.post.dto.PostUpdateCommand;
import nbc.chillguys.nebulazone.domain.post.entity.Post;
import nbc.chillguys.nebulazone.domain.post.service.PostDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@Service
@RequiredArgsConstructor
public class PostService {

	private final UserDomainService userDomainService;
	private final PostDomainService postDomainService;
	// todo: private final S3Service s3Service;

	public CreatePostResponse createPost(AuthUser authUser, CreatePostRequest request,
		List<MultipartFile> multipartFiles) {

		User findUser = userDomainService.findActiveUserById(authUser.getId());
		PostCreateCommand postCreateDto = PostCreateCommand.of(findUser, request);

		// todo: controller에서 넘어온 이미지들을 url 리스트로 변환한 다음 Post를 생성
		// List<String> postImageUrls = s3Service.createImageUrls(multipartFiles);
		// Post createPost = postDomainService.createPost(findUser, postCreateDto, postImageUrls);

		Post createPost = postDomainService.createPost(postCreateDto, new ArrayList<>());

		return CreatePostResponse.from(createPost, new ArrayList<>());

	}

	public UpdatePostResponse updatePost(PostUpdateCommand command) {
		Post post = postDomainService.updatePost(command);

		return UpdatePostResponse.from(post);
	}
}
