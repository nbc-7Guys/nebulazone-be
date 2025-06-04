package nbc.chillguys.nebulazone.domain.pointhistory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
	Optional<PointHistory> findActivePointHistoryById(Long id);
}
