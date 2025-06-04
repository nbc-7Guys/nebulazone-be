package nbc.chillguys.nebulazone.domain.auction.repository;

import java.util.List;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;

public interface AuctionCustomRepository {
	Page<AuctionFindInfo> findAuctionsWithProduct(int page, int size);

	List<AuctionFindInfo> finAuctionsBySortType(AuctionSortType sortType);

}
