package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionCustomRepository {

	Optional<Auction> findByProduct_IdAndIsDeletedFalse(long productId);
}
