package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.application.pointhistory.dto.request.AdminPointHistoryRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;

public interface PointHistoryCustomRepository {

	Page<AdminPointHistoryResponse> searchAdminPointHistories(AdminPointHistoryRequest request, Pageable pageable);

}
