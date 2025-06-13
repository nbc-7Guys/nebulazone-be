package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointHistoryAdminRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.AdminPointHistoryResponse;

public interface PointHistoryAdminRepositoryCustom {

	Page<AdminPointHistoryResponse> searchAdminPointHistories(PointHistoryAdminRequest request, Pageable pageable);

}
