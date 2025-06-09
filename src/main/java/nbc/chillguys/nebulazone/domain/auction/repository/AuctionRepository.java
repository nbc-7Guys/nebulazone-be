package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionCustomRepository {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select a from Auction a
		join fetch a.product p
		join fetch p.seller s
		where a.id = :auctionId and a.deleted = false
		""")
	Optional<Auction> findAuctionWithProductAndSellerLock(@Param("auctionId") Long id);

	Optional<Auction> findByIdAndDeletedFalse(Long id);
}
