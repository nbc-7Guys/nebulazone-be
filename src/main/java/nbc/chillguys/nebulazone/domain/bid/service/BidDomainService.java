package nbc.chillguys.nebulazone.domain.bid.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.repository.BidJdbcRepository;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidDomainService {

	private final BidJdbcRepository bidJdbcRepository;
	private final BidRepository bidRepository;

	@Transactional
	public void createAllBid(Auction auction, List<BidVo> bidVoList, Map<Long, User> userMap) {

		List<Bid> bidList = bidVoList.stream()
			.filter(bidVo -> userMap.containsKey(bidVo.getBidUserId()))
			.map(bidVo -> {
				User bidUser = userMap.get(bidVo.getBidUserId());

				return Bid.builder()
					.auction(auction)
					.user(bidUser)
					.price(bidVo.getBidPrice())
					.status(bidVo.getBidStatus())
					.build();
			})
			.toList();

		bidJdbcRepository.saveBidsBatch(bidList);

	}

	/**
	 * 특정 경매의 입찰 내역 조회
	 * @param auction 조회할 삭제되지 않은 경매
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

	/**
	 * 특정 경매의 최고가 입찰 조회<br>
	 * 유저를 함께 조회
	 * @param auctionId 경매 id
	 * @return 조회된 Bid
	 * @author 전나겸
	 */
	public Bid findHighBidByAuction(Long auctionId) {
		return bidRepository.findHighestPriceBidByAuctionWithUser(auctionId);
	}

	/**
	 * 특정 입찰 조회(유저도 함께 조회)
	 * @param bidId 조회할 입찰 Id
	 * @return 조회된 Bid
	 * @author 전나겸
	 */
	public Bid findBid(Long bidId) {
		return bidRepository.findBidWithWonUser(bidId)
			.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));
	}

	/**
	 * 해당 경매의 입찰 전체조회
	 * @param auctionId 조회할 경매
	 * @return 조회된 BidList
	 * @author 전나겸
	 */
	public List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId) {
		return bidRepository.findBidsByAuctionIdAndStatusBid(auctionId);
	}
}

