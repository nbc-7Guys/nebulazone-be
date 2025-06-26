package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

@Repository
@RequiredArgsConstructor
public class BidJdbcRepositoryImpl implements BidJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void saveBidsBatch(List<Bid> bids) {
		String sql = """
			insert into bids (auction_id, user_id, price, status, created_at, modified_at)
			values (?, ?, ?, ?, NOW(), NOW())
			""";

		List<Object[]> batchArgs = bids.stream()
			.map(bid -> new Object[] {
				bid.getAuction().getId(),
				bid.getUser().getId(),
				bid.getPrice(),
				bid.getStatus().name()
			})
			.toList();

		jdbcTemplate.batchUpdate(sql, batchArgs);
	}
}
