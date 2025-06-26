package nbc.chillguys.nebulazone.domain.auction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAuctionDomainService {

	private final AuctionRepository auctionRepository;

	/**
	 *
	 * @param auctionId
	 * @param bidPrice
	 * @return
	 */
	@Transactional
	public Auction autoEndAuction(Long auctionId, Long bidPrice) {
		Auction auction = auctionRepository.findAuctionWithProductAndSeller(auctionId)
			.filter(Auction::isNotWonAndNotDeleted)
			.orElse(null);

		if (auction == null) {
			log.warn("자동 낙찰 대상인 경매가 DB에 없습니다. 경매 id: {}", auctionId);
			return null;
		}

		if (bidPrice == 0L) {
			log.info("유찰 - 경매 id: {}", auctionId);
		} else {
			log.info("낙찰 - 경매 id: {}", auctionId);
			auction.wonAuction();
			auction.updateBidPrice(bidPrice);
		}
		return auction;
	}

}
