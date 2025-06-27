package nbc.chillguys.nebulazone.application.auction.scheduler;

import static nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

@Component
@RequiredArgsConstructor
public class AuctionFindAllBySortTypeScheduler {

	private final AuctionRedisService auctionRedisService;

	@Scheduled(cron = "0 0 */6 * * *")
	void refreshCache() {
		for (AuctionSortType sortType : values()) {
			auctionRedisService.findAuctionsBySortType(sortType);
		}
	}
}
