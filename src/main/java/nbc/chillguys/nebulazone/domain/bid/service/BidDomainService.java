package nbc.chillguys.nebulazone.domain.bid.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidDomainService {

	private final BidRepository bidRepository;

	/**
	 * 입찰 생성
	 * @param auction 경매
	 * @param user 입찰자
	 * @param price 입찰 가격
	 * @return Bid
	 * @author 전나겸
	 */
	@Transactional
	public Bid createBid(Auction auction, User user, Long price) {

		if (auction.getEndTime().isBefore(LocalDateTime.now())) {
			throw new AuctionException(AuctionErrorCode.AUCTION_CLOSED);
		}

		Bid bid = Bid.builder()
			.auction(auction)
			.user(user)
			.price(price)
			.build();

		return bidRepository.save(bid);
	}

	/**
	 * 특정 경매의 입찰 내역 조회
	 * @param auction 조회할 경매
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return FindBidInfo 페이징
	 * @author 전나겸
	 */
	public Page<FindBidInfo> findBids(Auction auction, int page, int size) {

		return bidRepository.findBidsWithUserByAuction(auction, page, size);
	}

	/**
	 * 내 입찰 내역 조회
	 * @param user 로그인 유저
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return FindBidInfo 페이징
	 * @author 전나겸
	 */
	public Page<FindBidInfo> findMyBids(User user, int page, int size) {

		return bidRepository.findMyBids(user, page, size);
	}
}
