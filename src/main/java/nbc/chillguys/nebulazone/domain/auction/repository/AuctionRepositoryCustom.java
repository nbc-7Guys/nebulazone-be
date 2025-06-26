package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

public interface AuctionRepositoryCustom {
	Page<AuctionFindAllInfo> findAuctionsWithProduct(int page, int size);

	List<AuctionFindAllInfo> finAuctionsBySortType(AuctionSortType sortType);

	Optional<Auction> findAuctionWithProductAndSeller(Long auctionId);

	Optional<Auction> findByAuctionWithProduct(Long auctionId);

	List<Auction> findAuctionsByNotDeletedAndIsWonFalse();

	Optional<AuctionFindDetailInfo> findAuctionDetail(Long auctionId);

}
