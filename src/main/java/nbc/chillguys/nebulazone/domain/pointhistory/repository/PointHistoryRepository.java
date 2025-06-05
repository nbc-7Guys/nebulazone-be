package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
	Optional<PointHistory> findActivePointHistoryById(Long id);

	List<PointHistory> findByUser_Id(Long userId);

	List<PointHistory> findByUser_IdAndPointHistoryStatus(Long userId, PointHistoryStatus status);

}
