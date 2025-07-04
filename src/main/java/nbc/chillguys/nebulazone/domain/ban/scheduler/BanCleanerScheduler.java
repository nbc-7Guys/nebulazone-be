package nbc.chillguys.nebulazone.domain.ban.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.ban.repository.BanRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BanCleanerScheduler {

	private final BanRepository banRepository;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	@SchedulerLock(name = "banCleanerScheduler")
	public void removeExpiredBans() {
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		banRepository.deleteAllExpired(now);
	}
}
