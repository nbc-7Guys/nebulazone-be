package nbc.chillguys.nebulazone.domain.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
