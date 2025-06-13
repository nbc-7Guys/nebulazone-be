package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long>, PointHistoryAdminRepositoryCustom {
	Optional<PointHistory> findActivePointHistoryById(Long id);

	List<PointHistory> findByUserId(Long userId);

	List<PointHistory> findByUserIdAndPointHistoryStatus(Long userId, PointHistoryStatus status);

	Page<PointHistory> findByUserId(Long userId, Pageable pageable);
}
