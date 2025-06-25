package nbc.chillguys.nebulazone.infra.redis.vo;

import java.time.LocalDateTime;
import java.util.UUID;

import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public record BidVo(
	String bidId,
	Long bidUserId,
	String bidUserNickname,
	String bidUserEmail,
	Long bidPrice,
	Long auctionId,
	String bidStatus,
	LocalDateTime bidCreatedAt
) {

	public static BidVo of(Long auctionId, User user, Long price) {
		return new BidVo(
			UUID.randomUUID().toString(),
			user.getId(),
			user.getNickname(),
			user.getEmail(),
			price,
			auctionId,
			BidStatus.BID.name(),
			LocalDateTime.now()
		);
	}
}
