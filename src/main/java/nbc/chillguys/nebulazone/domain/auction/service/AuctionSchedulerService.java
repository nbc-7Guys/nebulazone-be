package nbc.chillguys.nebulazone.domain.auction.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(30);
	private final Map<Long, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

	private final AuctionRepository auctionRepository;
	private final AutoAuctionDomainService autoAuctionDomainService;

	public void autoAuctionEndSchedule(Long auctionId, LocalDateTime endTime) {
		long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();

		if (seconds <= 0) {
			throw new AuctionException(AuctionErrorCode.AUCTION_END_TIME_INVALID);
		}

		ScheduledFuture<?> future = scheduler.schedule(() -> {
			autoAuctionDomainService.endAuction(auctionId);
			tasks.remove(auctionId);
		}, seconds, TimeUnit.SECONDS);

		tasks.put(auctionId, future);
	}

	public void cancelSchedule(Long auctionId) {
		ScheduledFuture<?> future = tasks.remove(auctionId);
		if (future != null) {
			future.cancel(true);
		}
	}

	@PostConstruct
	public void recoverSchedules() {
		log.info("서버 재시작 - 활성 경매 스케줄 복구 시작");
		List<Auction> auctionList = auctionRepository.findAllByDeletedFalse();

		auctionList.stream()
			.filter(auction -> !auction.isClosed())
			.filter(auction -> auction.getEndTime().isAfter(LocalDateTime.now()))
			.forEach(auction -> {

				autoAuctionEndSchedule(auction.getId(), auction.getEndTime());
			});
	}
}
