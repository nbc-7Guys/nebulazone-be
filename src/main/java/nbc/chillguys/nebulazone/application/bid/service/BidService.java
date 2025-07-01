package nbc.chillguys.nebulazone.application.bid.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindMyBidsResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.dto.FindBidsByAuctionInfo;
import nbc.chillguys.nebulazone.domain.bid.dto.FindMyBidsInfo;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@Service
@RequiredArgsConstructor
public class BidService {

	private final BidDomainService bidDomainService;
	private final AuctionDomainService auctionDomainService;

	private final AuctionRedisService auctionRedisService;
	private final BidRedisService bidRedisService;

	/**
	 * 특정 경매의 입찰 내역 조회
	 *
	 * @param auctionId 경매 id
	 * @param page 페이징
	 * @param size 사이즈
	 * @return 조회된 입찰 내역 응답값
	 * @author 전나겸
	 */
	public CommonPageResponse<FindBidResponse> findBidsByAuctionId(Long auctionId, int page, int size) {

		Page<FindBidResponse> findBidResponse = bidRedisService.findBidsByAuctionId(auctionId, page, size);

		if (!findBidResponse.isEmpty()) {
			return CommonPageResponse.from(findBidResponse);
		}

		auctionDomainService.existsAuctionByIdElseThrow(auctionId);

		Page<FindBidsByAuctionInfo> findBids = bidDomainService.findBidsByAuctionId(auctionId, page, size);

		Page<FindBidResponse> response = findBids.map(FindBidResponse::from);
		return CommonPageResponse.from(response);
	}

	/**
	 * 내 입찰 내역 조회
	 *
	 * @param user 로그인 유저
	 * @param page 페이징
	 * @param size 사이즈
	 * @return 조회된 내 입찰 내역 응답값
	 * @author 전나겸
	 */
	public CommonPageResponse<FindMyBidsResponse> findMyBids(User user, int page, int size) {

		List<FindMyBidsInfo> findBids = bidDomainService.findMyBids(user.getId());

		List<FindMyBidsResponse> allMyBids = new ArrayList<>(
			findBids.stream()
				.map(FindMyBidsResponse::from)
				.toList()
		);

		List<Long> auctionIds = auctionRedisService.findAllAuctionVoIds();
		List<BidVo> myBidVoList = bidRedisService.findMyBidVoList(user.getId(), auctionIds);

		for (BidVo bidVo : myBidVoList) {
			AuctionVo auctionVo = auctionRedisService.findRedisAuctionVo(bidVo.getAuctionId());

			if (auctionVo != null) {
				allMyBids.add(FindMyBidsResponse.of(bidVo, auctionVo));
			}
		}

		List<FindMyBidsResponse> pageContent = allMyBids.stream()
			.sorted(Comparator.comparing(FindMyBidsResponse::bidTime).reversed())
			.skip((long)page * size)
			.limit(size)
			.toList();

		Pageable pageable = PageRequest.of(page, size);

		Page<FindMyBidsResponse> findMyBidsResponses = new PageImpl<>(pageContent, pageable, allMyBids.size());
		return CommonPageResponse.from(findMyBidsResponses);
	}
}
