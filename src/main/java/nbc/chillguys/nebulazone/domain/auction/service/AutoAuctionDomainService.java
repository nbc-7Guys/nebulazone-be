package nbc.chillguys.nebulazone.domain.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;

@Service
@RequiredArgsConstructor
public class AutoAuctionDomainService {

	private final AuctionRepository auctionRepository;
	private final BidRepository bidRepository;

	@Transactional
	public void endAuction(Long auctionId) {
		
	}

}
