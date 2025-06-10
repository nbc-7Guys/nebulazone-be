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
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionDomainService {

	private final AuctionRepository auctionRepository;

	private final AuctionSchedulerService scheduler;

	/**
	 * 경매 생성
	 * @param command 경매상품, 종료시간
	 * @author 전나겸
	 */
	@Transactional
	public void createAuction(AuctionCreateCommand command) {

		Auction auction = Auction.builder()
			.product(command.product())
			.startPrice(command.product().getPrice())
			.endTime(command.endTime())
			.build();

		auctionRepository.save(auction);

		scheduler.recoverSchedules();
	}

	/**
	 * 경매 전체 조회(페이징)
	 * @param page 페이지 정보
	 * @param size 출력 개수
	 * @return 페이징 AuctionFindInfo
	 * @author 전나겸
	 */
	public Page<AuctionFindInfo> findAuctions(int page, int size) {

		return auctionRepository.findAuctionsWithProduct(page, size);

	}

	/**
	 * 경매 정렬 조건으로 조회<br>
	 * 마감 임박순 5개 조회, 경매 입찰 건수 많은 순 5개 조회
	 * @param sortType 정렬 조건(closing, popular)
	 * @return 리스트 AuctionFindInfo
	 */
	public List<AuctionFindInfo> findAuctionsBySortType(AuctionSortType sortType) {

		return auctionRepository.finAuctionsBySortType(sortType);

	}

	/**
	 * 경매 삭제(취소)
	 * @param auctionId 삭제할 경매 id
	 * @param user 로그인 유저
	 * @return 삭제된 경매 id
	 * @author 전나겸
	 */
	public Long deleteAuction(long auctionId, User user) {
		Auction findAuction = auctionRepository.findById(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

		if (findAuction.isDeleted()) {
			throw new AuctionException(AuctionErrorCode.ALREADY_DELETED_AUCTION);
		}

		if (findAuction.isAuctionOwner(user)) {
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
	 * 삭제되지 않은 경매
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

}
