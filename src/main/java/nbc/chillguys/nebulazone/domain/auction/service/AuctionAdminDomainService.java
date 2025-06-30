package nbc.chillguys.nebulazone.domain.auction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminInfo;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionAdminUpdateCommand;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;

@Service
@RequiredArgsConstructor
public class AuctionAdminDomainService {
	private final AuctionRepository auctionRepository;
	private final ProductDomainService productDomainService;

	/**
	 * 어드민 경매장 목록을 조건에 맞게 페이징 조회합니다.<br>
	 * 검색어, 삭제여부, 낙찰여부 등 필터를 지원합니다.
	 *
	 * @param command   도메인 계층 경매 조회 조건 DTO
	 * @param pageable  페이징 정보
	 * @return AdminAuctionInfo 목록 Page
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<AuctionAdminInfo> findAuctions(AuctionAdminSearchQueryCommand command, Pageable pageable) {
		return auctionRepository.searchAuctions(command, pageable)
			.map(AuctionAdminInfo::from);
	}

	/**
	 * 경매장 정보를 수정합니다.
	 *
	 * @param auctionId 수정 대상 경매 ID
	 * @param command   도메인 계층 경매 수정 커맨드
	 * @throws AuctionException 경매가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	@Transactional
	public void updateAuction(Long auctionId, AuctionAdminUpdateCommand command) {
		Auction auction = findByAuctionById(auctionId);
		auction.update(command.startPrice(), command.currentPrice(), command.endTime(), command.isWon());
		Product product = auction.getProduct();
		productDomainService.saveProductToEs(product);
	}

	/**
	 * 경매장을 소프트 딜리트(삭제) 처리합니다.<br>
	 * 복구가 가능합니다.
	 *
	 * @param auctionId 삭제 대상 경매 ID
	 * @throws AuctionException 경매가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	public void deleteAuction(Long auctionId) {
		Auction auction = findByAuctionById(auctionId);
		auction.delete(); // 소프트 딜리트: deleted=true, deletedAt=now
		productDomainService.deleteProductFromEs(auction.getProduct().getId());
	}

	/**
	 * 소프트 딜리트된 경매장을 복구합니다.
	 *
	 * @param auctionId 복구 대상 경매 ID
	 * @return 복구된 경매
	 * @throws AuctionException 경매가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	@Transactional
	public Auction restoreAuction(Long auctionId) {
		Auction auction = findByAuctionById(auctionId);
		auction.restore();
		Product product = auction.getProduct();
		productDomainService.saveProductToEs(product);
		return auction;
	}

	/**
	 * 경매 ID로 엔티티를 조회합니다.<br>
	 * 존재하지 않을 경우 AuctionException을 발생시킵니다.
	 *
	 * @param auctionId 조회할 경매 ID
	 * @return Auction 엔티티
	 * @throws AuctionException 경매가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	public Auction findByAuctionById(Long auctionId) {
		return auctionRepository.findById(auctionId)
			.orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
	}

}
