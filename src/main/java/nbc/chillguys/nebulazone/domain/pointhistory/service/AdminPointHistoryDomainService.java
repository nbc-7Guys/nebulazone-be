package nbc.chillguys.nebulazone.domain.pointhistory.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.pointhistory.dto.request.AdminPointHistoryRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;

@Service
@RequiredArgsConstructor
public class AdminPointHistoryDomainService {

	private final PointHistoryRepository pointHistoryRepository;

	public Page<AdminPointHistoryResponse> searchAdminPointHistories(
		AdminPointHistoryRequest request, Pageable pageable) {
		return pointHistoryRepository.searchAdminPointHistories(request, pageable);
	}
}
