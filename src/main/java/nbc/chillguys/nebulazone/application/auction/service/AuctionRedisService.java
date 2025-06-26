package nbc.chillguys.nebulazone.application.auction.service;

import static nbc.chillguys.nebulazone.application.auction.consts.AuctionConst.*;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionErrorCode;
import nbc.chillguys.nebulazone.domain.auction.exception.AuctionException;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;

@Service
@RequiredArgsConstructor
public class AuctionRedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	/**
	 * redis 경매 생성<br>
	 * hash로 auctionVo 저장, ZSet으로 경매 종료 순서를 관리
	 * @param redisAuctionDto 경매 생성을 위한 요청값
	 * @author 전나겸
	 */
	public void createAuction(CreateRedisAuctionDto redisAuctionDto) {

		Product product = redisAuctionDto.product();
		Auction auction = redisAuctionDto.auction();
		User user = redisAuctionDto.user();
		ProductEndTime productEndTime = redisAuctionDto.ProductEndTime();

		String auctionKey = AUCTION_PREFIX + auction.getId();
		AuctionVo auctionVo = AuctionVo.of(product, auction, user);

		Map<String, Object> auctionVoMap = objectMapper.convertValue(auctionVo, new TypeReference<>() {
		});
		redisTemplate.opsForHash().putAll(auctionKey, auctionVoMap);

		long endTimestamp = System.currentTimeMillis() / 1000 + productEndTime.getSeconds();
		redisTemplate.opsForZSet().add(AUCTION_ENDING_PREFIX, auction.getId(), endTimestamp);

	}

	/**
	 * 특정 경매 조회<br>
	 * AuctionVo 반환
	 * @param auctionId 조회할 경매 id
	 * @return AuctionVo
	 * @author 전나겸
	 */
	public AuctionVo getAuctionVo(Long auctionId) {
		Map<Object, Object> auctionMap = redisTemplate.opsForHash()
			.entries(AUCTION_PREFIX + auctionId);

		if (auctionMap.isEmpty()) {
			throw new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND);
		}

		return objectMapper.convertValue(auctionMap, AuctionVo.class);
	}

	/**
	 * 특정 경매의 입찰 최고가 갱신
	 * @param auctionId 대상 경매 id
	 * @param bidPrice 갱신할 입찰가 (null 가능 - 입찰이 모두 취소된 경우)
	 * @author 전나겸
	 */
	public void updateAuctionCurrentPrice(Long auctionId, Long bidPrice) {
		String auctionKey = "auction:" + auctionId;

		redisTemplate.opsForHash().put(auctionKey, "currentPrice", bidPrice);

	}

	// todo : 수동 낙찰

	// todo : 자동 낙찰 -> 자동낙찰은 스케줄러로 구현하고 따로 처리

	// todo : 내 경매 내역 조회

	// todo : 경매 상세 조회

	// todo : 내 경매 삭제

	// todo : 정렬 기반 경매 조회

}
