package nbc.chillguys.nebulazone.domain.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
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

	public Page<AuctionFindInfo> findAuctions(int page, int size) {

		return auctionRepository.findAuctionsWithProduct(page, size);

	}

	public List<AuctionFindInfo> findAuctionsBySortType(AuctionSortType sortType) {

		return auctionRepository.finAuctionsBySortType(sortType);

	}

}
