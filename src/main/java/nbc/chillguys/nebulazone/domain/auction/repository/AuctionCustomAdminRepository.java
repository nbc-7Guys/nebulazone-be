package nbc.chillguys.nebulazone.domain.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.auction.dto.AdminAuctionSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionCustomAdminRepository {
	Page<Auction> searchAuctions(AdminAuctionSearchQueryCommand command, Pageable pageable);
}
