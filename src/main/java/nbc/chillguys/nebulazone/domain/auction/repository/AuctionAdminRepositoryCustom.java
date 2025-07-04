package nbc.chillguys.nebulazone.domain.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;

public interface AuctionAdminRepositoryCustom {
	Page<Auction> searchAuctions(AuctionAdminSearchQueryCommand command, Pageable pageable);
}
