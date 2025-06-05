package nbc.chillguys.nebulazone.domain.bid.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;

public interface BidCustomRepository {

	Page<FindBidInfo> findBidsWithUserByAuction(Auction auction, int page, int size);
}
