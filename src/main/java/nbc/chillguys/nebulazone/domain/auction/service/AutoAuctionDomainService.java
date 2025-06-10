package nbc.chillguys.nebulazone.domain.auction.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoAuctionDomainService {

	private final AuctionRepository auctionRepository;
	private final BidRepository bidRepository;

	@Transactional
	public void endAuction(Long auctionId) {
		Optional<Auction> optAuction = auctionRepository.findById(auctionId);

		if (optAuction.isEmpty()) {
			log.warn("자동 종료할 경매를 찾을 수 없음. 경매 id: {}", auctionId);
			return;
		}

		Auction endedauction = optAuction.get();

		if (endedauction.isClosed() || endedauction.isDeleted()) {
			log.info("이미 종료된 경매를 자동 종료 할 수 없음. 경매 id: {}", auctionId);
			return;
		}

		Optional<Bid> optBid = bidRepository.findHighestPriceBidByAuction(endedauction);
		if (optBid.isEmpty()) {
			log.info("유찰 - 경매 id: {}", auctionId);
			endedauction.close();

		} else {
			Bid wonBid = optBid.get();
			log.info("낙찰 - 경매 id: {}, 입찰 id: {}", auctionId, wonBid.getId());
			wonBid.wonBid();
			endedauction.getProduct().purchase();
			endedauction.close();
		}

	}

}
