package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public interface BidRepositoryCustom {

	List<FindMyBidsInfo> findMyBids(Long userId);

	Bid findHighestPriceBidByAuctionWithUser(Long auctionId);

	Page<FindBidsByAuctionInfo> findBidsWithUserByAuctionId(Long auctionId, int page, int size);
}
