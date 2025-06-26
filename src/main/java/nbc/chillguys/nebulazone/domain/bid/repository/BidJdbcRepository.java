package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public interface BidJdbcRepository {
	void saveBidsBatch(List<Bid> bids);
}
