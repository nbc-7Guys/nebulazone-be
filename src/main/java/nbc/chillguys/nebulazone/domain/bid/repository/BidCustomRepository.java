package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface BidCustomRepository {

	Page<FindBidInfo> findBidsWithUserByAuction(Auction auction, int page, int size);

	Page<FindBidInfo> findMyBids(User user, int page, int size);

	Optional<Long> findHighestPriceByAuction(Auction auction);
}
