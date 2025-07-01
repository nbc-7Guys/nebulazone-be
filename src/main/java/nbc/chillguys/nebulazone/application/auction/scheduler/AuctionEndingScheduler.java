package nbc.chillguys.nebulazone.application.auction.scheduler;

import static nbc.chillguys.nebulazone.application.auction.consts.AuctionConst.*;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AutoAuctionRedisService;

@Component
@RequiredArgsConstructor
public class AuctionEndingScheduler {

	private final RedisTemplate<String, Object> redisTemplate;
	private final AutoAuctionRedisService autoAuctionRedisService;

	/**
	 * 1초 마다 redis의 ZSet에 등록된 경매 데이터의 종료시점과 현재 시간을 비교하여 자동 낙찰을 수행
	 */
	@Scheduled(fixedDelay = 1000)
	@SchedulerLock(name = "auctionEndingScheduler")
	public void processEndedAuctions() {
		long nowTimeStamp = System.currentTimeMillis() / 1000;

		Set<Object> object = redisTemplate.opsForZSet().rangeByScore(AUCTION_ENDING_PREFIX, 0, nowTimeStamp);

		Optional.ofNullable(object)
			.orElse(Set.of())
			.stream()
			.map(Object::toString)
			.map(Long::valueOf)
			.forEach(autoAuctionRedisService::processAuctionEnding);

	}

}
