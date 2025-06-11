package nbc.chillguys.nebulazone.domain.auction.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindAllInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.ManualEndAuctionInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionDomainService {

	private final AuctionRepository auctionRepository;

	/**
	 * 경매 생성
	 * @param command 경매상품, 종료시간
	 * @author 전나겸
	 */
	@Transactional
	public Auction createAuction(AuctionCreateCommand command) {

		Auction auction = Auction.builder()
			.product(command.product())
			.startPrice(command.product().getPrice())
			.endTime(command.endTime())
			.build();

		return auctionRepository.save(auction);
	}

	/**
	 * 경매 전체 조회(페이징)
	 * @param page 페이지 정보
	 * @param size 출력 개수
	 * @return 페이징 AuctionFindInfo
	 * @author 전나겸
	 */
	public Page<AuctionFindAllInfo> findAuctions(int page, int size) {

		return auctionRepository.findAuctionsWithProduct(page, size);

	}

	/**
	 * 경매 정렬 조건으로 조회<br>
	 * 마감 임박순 5개 조회, 경매 입찰 건수 많은 순 5개 조회
	 * @param sortType 정렬 조건(closing, popular)
	 * @return 리스트 AuctionFindInfo
	 */
	public List<AuctionFindAllInfo> findAuctionsBySortType(AuctionSortType sortType) {

		return auctionRepository.finAuctionsBySortType(sortType);

	}

	/**
	 * 경매 상세 조회
	 * @param auctionId 조회할 경매
	 * @return 상품 등록자, 상품정보, 입찰 건수 정보가 포함된 경매 조회
	 * @author 전나겸
	 */
	public AuctionFindDetailInfo findAuction(Long auctionId) {

		return auctionRepository.findAuctionDetail(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

	}

	/**
	 * 수동 낙찰
	 * @param loginUser 로그인 유저
	 * @param wonBid 낙찰 대상인 입찰
	 * @param auctionId 종료할 경매 id
	 * @return manualEndAuctionInfo
	 * @author 전나겸
	 */
	@Transactional
	public ManualEndAuctionInfo manualEndAuction(User loginUser, Bid wonBid, Long auctionId) {

		Auction findAuction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

		if (findAuction.isDeleted()) {
			throw new AuctionException(AuctionErrorCode.ALREADY_DELETED_AUCTION);
		}

		if (!findAuction.isAuctionOwner(loginUser)) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_OWNER);
		}

		if (!Objects.equals(findAuction.getCurrentPrice(), wonBid.getPrice())) {
			throw new AuctionException(AuctionErrorCode.MISMATCH_BID_PRICE);
		}

		wonBid.wonBid();
		findAuction.wonAuction();
		findAuction.updateEndTime();

		return ManualEndAuctionInfo.from(findAuction, wonBid, loginUser);
	}

	/**
	 * 경매 삭제(취소)
	 * @param auctionId 삭제할 경매 id
	 * @param user 로그인 유저
	 * @return 삭제된 경매 id
	 * @author 전나겸
	 */
	@Transactional
	public Long deleteAuction(Long auctionId, User user) {
		Auction findAuction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

		if (findAuction.isDeleted()) {
			throw new AuctionException(AuctionErrorCode.ALREADY_DELETED_AUCTION);
		}

		if (!findAuction.isAuctionOwner(user)) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_OWNER);
		}

		return findAuction.delete();
	}

	/**
	 * 삭제되지 않은 경매 단건 조회
	 * @param productId 판매 상품 id
	 * @return auction
	 * @author 윤정환
	 */
	public Auction findAuctionByProductId(Long productId) {
		return auctionRepository.findByProduct_IdAndDeletedFalse(productId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

	/**
	 * 삭제되지 않은 경매 조회
	 * @param auctionId 조회할 AuctionId
	 * @return 조회된 경매
	 * @author 전나겸
	 */
	public Auction findActiveAuctionById(Long auctionId) {
		return auctionRepository.findByIdAndDeletedFalse(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

	/**
	 * 삭제되지 않은 비관적 락이 적용된 경매 조회(상품, 판매자 정보 한번에 조회)
	 * @param auctionId 조회할 AuctionId
	 * @return 비관적 락이 적용된 auction
	 * @author 전나겸
	 */
	@Transactional
	public Auction findActiveAuctionWithProductAndSellerLock(Long auctionId) {
		return auctionRepository.findAuctionWithProductAndSellerLock(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

	/**
	 * 삭제 되지 않은 경매 리스트 조회
	 * @return 삭제 되지 않은 경매 리스트
	 * @author 전나겸
	 */
	public List<Auction> findActiveAuctionsWithProductAndSeller() {
		return auctionRepository.findAuctionsByNotDeletedAndIsWonFalse();
	}

}
