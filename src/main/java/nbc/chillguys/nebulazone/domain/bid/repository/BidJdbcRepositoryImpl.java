package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BidJdbcRepositoryImpl implements BidJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void saveBidsBatch(List<Object[]> bids) {
		String sql = """
			insert into bids (auction_id, user_id, price, status, created_at, modified_at)
			values (?, ?, ?, ?, ?, now())
			""";

		jdbcTemplate.batchUpdate(sql, bids);
	}
}
