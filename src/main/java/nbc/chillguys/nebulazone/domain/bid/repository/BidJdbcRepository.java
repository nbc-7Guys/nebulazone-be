package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

public interface BidJdbcRepository {
	void saveBidsBatch(List<Object[]> bidArr);
}
