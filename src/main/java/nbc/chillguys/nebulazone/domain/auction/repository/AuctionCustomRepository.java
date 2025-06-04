package nbc.chillguys.nebulazone.domain.auction.repository;

import org.springframework.data.domain.Page;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;

public interface AuctionCustomRepository {
	Page<AuctionFindInfo> findAllAuctionsWithProduct(int page, int size);
}
