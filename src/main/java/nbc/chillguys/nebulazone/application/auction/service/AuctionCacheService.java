package nbc.chillguys.nebulazone.application.auction.service;

import java.time.Duration;
import java.util.List;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindSortTypeAuctionResponse;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;

// 생각해보니 redis에서 모든 실시간 데이터가 관리되는데 rdb 캐싱이 의미 없어짐 -> 고쳐야함
@Service
@RequiredArgsConstructor
public class AuctionCacheService {

	private static final String AUCTION_CACHE_PREFIX = "auction:sorted:";
	private static final Duration AUCTION_CACHE_TTL = Duration.ofHours(6);

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;
	private final AuctionDomainService auctionDomainService;
	private final RedissonClient redissonClient;

	public FindSortTypeAuctionResponse findAuctionsBySortType(AuctionSortType sortType) {
		String cacheKey = AUCTION_CACHE_PREFIX + sortType.name();

		Object cached = redisTemplate.opsForValue().get(cacheKey);

		if (cached != null) {
			return objectMapper.convertValue(cached, FindSortTypeAuctionResponse.class);
		}

		RLock lock = redissonClient.getLock(cacheKey + ":lock");
		lock.lock();
		try {
			cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				return objectMapper.convertValue(cached, FindSortTypeAuctionResponse.class);
			}
			return findAuctionsBySortTypeAndSaveRedis(sortType, cacheKey);
		} finally {
			lock.unlock();
		}
	}

	@Scheduled(cron = "0 0 */6 * * *")
	public void refreshCache() {
		for (AuctionSortType sortType : AuctionSortType.values()) {

			String cacheKey = AUCTION_CACHE_PREFIX + sortType.name();
			findAuctionsBySortTypeAndSaveRedis(sortType, cacheKey);
		}
	}

	private FindSortTypeAuctionResponse findAuctionsBySortTypeAndSaveRedis(AuctionSortType sortType, String cacheKey) {
		List<AuctionFindAllInfo> auctionsBySortType = auctionDomainService.findAuctionsBySortType(sortType);
		FindSortTypeAuctionResponse response = FindSortTypeAuctionResponse.from(auctionsBySortType);
		redisTemplate.opsForValue().set(cacheKey, response, AUCTION_CACHE_TTL);
		return response;
	}
}
