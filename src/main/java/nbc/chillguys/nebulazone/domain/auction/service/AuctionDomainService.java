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
	 * 경매 삭제
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

		return findAuction.deleteAuction();
	}

	/**
	 * 삭제되지 않은 경매
	 * @param id 경매 id
	 * @return 조회된 경매
	 * @author 전나겸
	 */
	public Auction findActiveAuctionById(Long id) {
		return auctionRepository.findByIdAndDeletedFalse(id)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

	/**
	 * 상품과 판매자 정보를 함께 조회한 비관적 락이 적용된 경매
	 * @param id 경매 id
	 * @return 락이 적용된 경매
	 * @author 전나겸
	 */
	@Transactional
	public Auction findActiveAuctionWithProductAnsSellerLock(Long id) {
		return auctionRepository.findAuctionWithProductAndSellerLock(id)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

}
