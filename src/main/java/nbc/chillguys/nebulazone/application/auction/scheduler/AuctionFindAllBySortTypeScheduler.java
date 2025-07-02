package nbc.chillguys.nebulazone.application.auction.scheduler;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.infra.redis.constant.AuctionConstants;

@Component
@RequiredArgsConstructor
public class AuctionFindAllBySortTypeScheduler {

	private final AuctionRedisService auctionRedisService;
	private final RedisTemplate<String, Object> redisTemplate;

	@Scheduled(cron = "0 */5 * * * *")
	@SchedulerLock(name = "auctionCacheScheduler")
	void refreshCache() {
		for (AuctionSortType sortType : AuctionSortType.values()) {
			redisTemplate.delete(AuctionConstants.AUCTION_FIND_SORT_TYPE_PREFIX + sortType.name());
			auctionRedisService.findAuctionsBySortType(sortType);
		}
	}
}
