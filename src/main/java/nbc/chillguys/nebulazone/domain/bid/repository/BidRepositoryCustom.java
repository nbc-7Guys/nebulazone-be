package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface BidRepositoryCustom {

	Page<FindMyBidsInfo> findMyBids(User user, int page, int size);

	Bid findHighestPriceBidByAuctionWithUser(Long auctionId);

	Optional<Bid> findBidWithWonUser(Long bidId);

	List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId);

	Page<FindBidsByAuctionInfo> findBidsWithUserByAuctionId(Long auctionId, int page, int size);
}
