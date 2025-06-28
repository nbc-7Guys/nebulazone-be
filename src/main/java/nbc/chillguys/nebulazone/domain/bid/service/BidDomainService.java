package nbc.chillguys.nebulazone.domain.bid.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
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

	public List<FindMyBidsInfo> findMyBids(Long userId) {

		return bidRepository.findMyBids(userId);
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
	 * 해당 경매의 입찰 전체조회
	 * @param auctionId 조회할 경매
	 * @return 조회된 BidList
	 * @author 전나겸
	 */
	public List<Bid> findBidsByAuctionIdAndStatusBid(Long auctionId) {
		return bidRepository.findBidsByAuctionIdAndStatusBid(auctionId);
	}

	/**
	 * 특정 경매의 입찰 내역 조회
	 * @param auctionId 대상 경매 id
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return FindBidInfo 페이징
	 * @author 전나겸
	 */
	public Page<FindBidsByAuctionInfo> findBidsByAuctionId(Long auctionId, int page, int size) {
		return bidRepository.findBidsWithUserByAuctionId(auctionId, page, size);
	}
}

