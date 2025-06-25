package nbc.chillguys.nebulazone.application.auction.service;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.dto.CreateRedisAuctionDto;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;

@Service
@RequiredArgsConstructor
public class AuctionRedisService {

	private static final String AUCTION_PREFIX = "auction:";
	private static final String AUCTION_ENDING_KEY = "auction:ending";

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

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
		redisTemplate.opsForZSet().add(AUCTION_ENDING_KEY, auction.getId(), endTimestamp);

	}
}
