package nbc.chillguys.nebulazone.domain.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewInfo;
import nbc.chillguys.nebulazone.domain.review.dto.AdminReviewSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class AdminReviewDomainService {
	private final ReviewRepository reviewRepository;

	public Page<AdminReviewInfo> findReviews(AdminReviewSearchQueryCommand command, Pageable pageable) {
		return reviewRepository.searchReviews(command, pageable)
			.map(AdminReviewInfo::from);
	}
}
