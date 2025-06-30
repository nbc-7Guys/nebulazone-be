package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.List;
import java.util.Optional;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionRepositoryCustom {
	Optional<Auction> findAuctionWithProductAndSeller(Long auctionId);

	Optional<Auction> findByAuctionWithProduct(Long auctionId);

	List<Auction> findAuctionsByNotDeletedAndIsWonFalse();

	Optional<AuctionFindDetailInfo> findAuctionDetail(Long auctionId);

}
