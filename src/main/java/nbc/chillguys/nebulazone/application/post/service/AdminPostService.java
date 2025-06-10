package nbc.chillguys.nebulazone.application.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.AdminPostSearchRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.AdminPostResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostInfo;
import nbc.chillguys.nebulazone.domain.post.dto.AdminPostSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.post.service.AdminPostDomainService;

@Service
@RequiredArgsConstructor
public class AdminPostService {

	private final AdminPostDomainService adminPostDomainService;

	public CommonPageResponse<AdminPostResponse> findPosts(AdminPostSearchRequest request, Pageable pageable) {
		AdminPostSearchQueryCommand command = new AdminPostSearchQueryCommand(
			request.keyword(),
			request.type(),
			request.includeDeleted()
		);
		Page<AdminPostInfo> infoPage = this.adminPostDomainService.findPosts(command, pageable);
		return CommonPageResponse.from(infoPage.map(AdminPostResponse::from));
	}

}
