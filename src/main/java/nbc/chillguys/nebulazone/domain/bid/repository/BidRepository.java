package nbc.chillguys.nebulazone.domain.bid.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, Long>, BidCustomRepository {

	@Query("select max(b.price) from Bid b where b.auction = :auction")
	Optional<Long> findHighestPriceByAuction(Auction auction);
}
