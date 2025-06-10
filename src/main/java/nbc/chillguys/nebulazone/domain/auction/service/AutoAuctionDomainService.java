package nbc.chillguys.nebulazone.domain.auction.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAuctionDomainService {

	private final AuctionRepository auctionRepository;

	@Transactional
	public void endAuction(Long auctionId, Bid wonBid) {
		Optional<Auction> optAuction = auctionRepository.findById(auctionId);

		if (optAuction.isEmpty()) {
			log.warn("자동 종료할 경매를 찾을 수 없음. 경매 id: {}", auctionId);
			return;
		}

		Auction endedAuction = optAuction.get();

		if (endedAuction.isWon() || endedAuction.isDeleted()) {
			log.warn("경매가 낙찰되었거나 종료 상태인 경매는 자동 종료 불가. 경매 id: {}", auctionId);
			return;
		}

		if (wonBid == null) {
			log.info("유찰 - 경매 id: {}", auctionId);
			return;
		}

		log.info("낙찰 - 경매 id: {}, 입찰 id: {}", auctionId, wonBid.getId());
		wonBid.wonBid();
		endedAuction.getProduct().purchase();
		endedAuction.wonAuction();

	}

}
