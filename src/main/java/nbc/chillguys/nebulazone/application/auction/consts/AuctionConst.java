package nbc.chillguys.nebulazone.application.auction.consts;

import java.time.Duration;

public interface AuctionConst {
	String AUCTION_PREFIX = "auction:";
	String AUCTION_ENDING_PREFIX = "auction:ending";
	String AUCTION_LOCK_ENDING_PREFIX = "auction:ending:lock";
	String AUCTION_LOCK_DELETE_PREFIX = "auction:delete:lock";
	String AUCTION_FIND_SORT_TYPE_PREFIX = "auction:sorted:";
	Duration AUCTION_FIND_SORT_TYPE_TTL = Duration.ofHours(6);

}
