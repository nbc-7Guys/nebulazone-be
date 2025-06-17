package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface BidRepositoryCustom {

	Page<FindBidInfo> findBidsWithUserByAuction(Auction auction, int page, int size);

	Page<FindBidInfo> findMyBids(User user, int page, int size);

	Optional<Long> findActiveBidHighestPriceByAuction(Auction auction);

	Bid findHighestPriceBidByAuctionWithUser(Long auctionId);

	Optional<Bid> findBidWithWonUser(Long bidId);

	Optional<Bid> findBidByAuctionIdAndUserId(Long auctionId, Long userId);

	List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId);
}
