package nbc.chillguys.nebulazone.domain.auction.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionCreateCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;

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
			.currentPrice(0L)
			.endTime(command.endTime())
			.build();

		return auctionRepository.save(auction);
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

	@Transactional
	public Auction manualEndAuction(Long auctionId, Long bidPrice) {

		Auction auction = auctionRepository.findAuctionWithProductAndSeller(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

		auction.validDeleted();

		auction.wonAuction();
		auction.updateBidPrice(bidPrice);
		auction.updateEndTime();

		return auction;
	}

	/**
	 * 경매 삭제 == 경매 취소(수동 유찰)
	 * @param auctionId 삭제할 경매 id
	 * @return 삭제된 경매 id
	 * @author 전나겸
	 */
	@Transactional
	public Auction deleteAuction(Long auctionId) {
		Auction auction = auctionRepository.findByAuctionWithProduct(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

		auction.validDeleted();

		auction.delete();

		return auction;
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
	 * 경매가 RDB에 없으면 NotFound 에러 발생
	 * @param auctionId 확인할 경매 id
	 * @author 전나겸
	 */
	public void existsAuctionByIdElseThrow(Long auctionId) {
		if (!auctionRepository.existsById(auctionId)) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND);
		}
	}

	/**
	 * 진행 중인 모든 경매 조회 (Redis 동기화용)
	 * @return 삭제되지 않고 낙찰되지 않은 경매 목록
	 * @author 전나겸
	 */
	public List<Auction> findActiveAuctions() {
		return auctionRepository.findAuctionsByNotDeletedAndIsWonFalse();
	}

}
