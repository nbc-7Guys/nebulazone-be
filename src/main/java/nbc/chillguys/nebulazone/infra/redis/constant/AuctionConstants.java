package nbc.chillguys.nebulazone.infra.redis.constant;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuctionConstants {
	public static final String AUCTION_PREFIX = "auction:";
	public static final String AUCTION_ENDING_PREFIX = "auction:ending";
	public static final String AUCTION_FIND_SORT_TYPE_PREFIX = "auction:sorted:";
	public static final Duration AUCTION_FIND_SORT_TYPE_TTL = Duration.ofHours(6);
}
