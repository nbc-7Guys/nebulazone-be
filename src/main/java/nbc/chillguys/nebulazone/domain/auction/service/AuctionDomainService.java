package nbc.chillguys.nebulazone.domain.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionDomainService {

	private final AuctionRepository auctionRepository;

	@Transactional
	public void createAuction(AuctionCreateCommand command) {

		auctionRepository.save(Auction.of(command.product(), command.startPrice(), command.endTime()));
	}

}
