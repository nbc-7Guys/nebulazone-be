package nbc.chillguys.nebulazone.domain.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.BidAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.entity.BidStatus;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;

@Service
@RequiredArgsConstructor
public class BidAdminDomainService {
	private final BidRepository bidRepository;

	/**
	 * 어드민 입찰 내역을 조건에 맞게 페이징 조회합니다.<br>
	 * 경매ID, 유저ID, 상태 등으로 검색 필터링 가능합니다.
	 *
	 * @param command   도메인 계층 검색 조건 커맨드
	 * @param pageable  페이징 정보
	 * @return AdminBidInfo 목록 Page
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<BidAdminInfo> findBids(BidAdminSearchQueryCommand command, Pageable pageable) {
		return bidRepository.searchBids(command, pageable)
			.map(BidAdminInfo::from);
	}

	/**
	 * 입찰 상태를 변경합니다.<br>
	 * (예: 입찰→취소, 입찰→낙찰 등)
	 *
	 * @param bidId     상태 변경 대상 입찰 ID
	 * @param status    변경할 BidStatus 값
	 * @throws BidException 입찰이 존재하지 않을 경우 발생
	 * @author 정석현
	 */
	@Transactional
	public void updateBidStatus(Long bidId, BidStatus status) {
		Bid bid = findByBidId(bidId);
		bid.updateStatus(status);
	}

	/**
	 * 입찰을 소프트델리트합니다.<br>
	 *
	 * @param bidId     삭제할 입찰 ID
	 * @throws BidException 입찰이 존재하지 않을 경우 발생
	 * @author 정석현
	 */
	@Transactional
	public void deleteBid(Long bidId) {
		Bid bid = findByBidId(bidId);
		bid.cancelBid();
	}

	/**
	 * 입찰 ID로 Bid 엔티티를 조회합니다.<br>
	 * 없으면 BidException 발생
	 *
	 * @param bidId     조회할 입찰 ID
	 * @return Bid 엔티티
	 * @throws BidException 입찰이 존재하지 않을 경우 발생
	 * @author 정석현
	 */
	public Bid findByBidId(Long bidId) {
		return bidRepository.findById(bidId)
			.orElseThrow(() -> new BidException(BidErrorCode.BID_NOT_FOUND));
	}
}
