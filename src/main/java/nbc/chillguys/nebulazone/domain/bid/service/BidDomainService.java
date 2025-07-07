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

	/**
	 * 입찰 배치 저장
	 *
	 * @param auction 대상 경매
	 * @param bidVoList 저장할 BidVo 리스트
	 * @param userMap 입찰 유저 정보 <유저 아이디, 유저>
	 * @author 전나겸
	 */
	@Transactional
	public void createAllBid(Auction auction, List<BidVo> bidVoList, Map<Long, User> userMap) {

		List<Object[]> batchArgs = bidVoList.stream()
			.filter(bidVo -> userMap.containsKey(bidVo.getBidUserId()))
			.map(bidVo -> new Object[] {
				auction.getId(),
				bidVo.getBidUserId(),
				bidVo.getBidPrice(),
				bidVo.getBidStatus(),
				bidVo.getBidCreatedAt()
			})
			.toList();

		bidJdbcRepository.saveBidsBatch(batchArgs);

	}

	/**
	 * 내 입찰 내역 전체 조회
	 *
	 * @param userId 로그인 유저 아이디
	 * @return 연관관계 대상 한번에 조회한 입찰 내역 리스트
	 * @author 전나겸
	 */
	public List<FindMyBidsInfo> findMyBids(Long userId) {
		return bidRepository.findMyBids(userId);
	}

	/**
	 * 특정 경매의 최고가 입찰 조회<br>
	 * 유저를 함께 조회
	 *
	 * @param auctionId 경매 id
	 * @return 조회된 Bid
	 * @author 전나겸
	 */
	public Bid findWonBidByAuctionId(Long auctionId) {
		return bidRepository.findHighestPriceBidByAuctionWithUser(auctionId);
	}

	/**
	 * 특정 경매의 입찰 내역 조회
	 *
	 * @param auctionId 대상 경매 id
	 * @param page 페이지
	 * @param size 출력 개수
	 * @return FindBidInfo 페이징
	 * @author 전나겸
	 */
	public Page<FindBidsByAuctionInfo> findBidsByAuctionId(Long auctionId, int page, int size) {
		return bidRepository.findBidsWithUserByAuctionId(auctionId, page, size);
	}

	/**
	 * 데이터베이스에 백업할 redis의 입찰 내역들
	 *
	 * @param bids 저장할 입찰 리스트
	 * @author 전나겸
	 */
	@Transactional
	public void saveAllBids(List<Bid> bids) {
		bidRepository.saveAll(bids);
	}

	/**
	 * 복구할 입찰 내역 조회
	 *
	 * @param auctionId 복구할 대상 경매 id
	 * @return 복구한 입찰 리스트
	 * @author 전나겸
	 */
	@Transactional(readOnly = true)
	public List<Bid> findActiveBidsForRecovery(Long auctionId) {
		return bidRepository.findActiveBidsByAuctionId(auctionId);
	}
}

