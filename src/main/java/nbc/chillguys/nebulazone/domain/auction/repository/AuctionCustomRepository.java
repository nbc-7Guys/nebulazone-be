package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

public interface AuctionCustomRepository {
	Page<AuctionFindInfo> findAuctionsWithProduct(int page, int size);

	List<AuctionFindInfo> finAuctionsBySortType(AuctionSortType sortType);

	Optional<Auction> findAuctionWithProductAndSellerLock(@Param("auctionId") Long id);
}
