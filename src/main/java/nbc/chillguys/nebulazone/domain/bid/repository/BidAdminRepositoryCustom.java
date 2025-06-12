package nbc.chillguys.nebulazone.domain.bid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.bid.dto.AdminBidSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public interface BidAdminRepositoryCustom {
	Page<Bid> searchBids(AdminBidSearchQueryCommand command, Pageable pageable);
}
