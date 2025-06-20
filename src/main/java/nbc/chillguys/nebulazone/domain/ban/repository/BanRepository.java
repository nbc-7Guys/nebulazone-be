package nbc.chillguys.nebulazone.domain.ban.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.ban.entity.Ban;

public interface BanRepository extends JpaRepository<Ban, Long>, BanRepositoryCustom {
	Optional<Ban> findByIpAddress(String ipAddress);

	int deleteByExpiresAtBefore(LocalDateTime now);
}
