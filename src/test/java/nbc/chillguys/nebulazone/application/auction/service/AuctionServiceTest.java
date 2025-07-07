package nbc.chillguys.nebulazone.application.auction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidRedisService;
import nbc.chillguys.nebulazone.domain.auction.dto.AuctionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.vo.AuctionVo;
import nbc.chillguys.nebulazone.infra.redis.vo.BidVo;

@DisplayName("경매 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

	@Mock
	private AuctionDomainService auctionDomainService;

	@Mock
	private BidDomainService bidDomainService;

	@Mock
	private AuctionRedisService auctionRedisService;

	@Mock
	private BidRedisService bidRedisService;

	@InjectMocks
	private AuctionService auctionService;

	private AuctionVo auctionVo;
	private BidVo bidVo;
	private AuctionFindDetailInfo auctionFindDetailInfo;
	private Bid bid;
	private User user;
	private Auction auction;

	@BeforeEach
	void init() {

		user = User.builder()
			.email("bidder@test.com")
			.nickname("입찰자닉네임")
			.build();
		ReflectionTestUtils.setField(user, "id", 2L);

		User seller = User.builder()
			.email("seller@test.com")
			.nickname("판매자닉네임")
			.build();
		ReflectionTestUtils.setField(seller, "id", 1L);

		Product product = Product.builder()
			.name("테스트 상품")
			.description("테스트 상품 설명")
			.price(100000L)
			.seller(seller)
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);

		auction = Auction.builder()
			.product(product)
			.startPrice(100000L)
			.currentPrice(120000L)
			.endTime(LocalDateTime.now().plusDays(1))
			.isWon(false)
			.build();
		ReflectionTestUtils.setField(auction, "id", 1L);

		auctionVo = AuctionVo.of(product, auction, seller, List.of("image1.jpg"));

		bidVo = BidVo.of(1L, user, 120000L);

		bid = Bid.builder()
			.auction(auction)
			.user(user)
			.price(120000L)
			.status("BID")
			.build();

		auctionFindDetailInfo = new AuctionFindDetailInfo(
			1L, 1L, "판매자닉네임", "seller@test.com",
			100000L, 120000L, false, LocalDateTime.now().plusDays(1),
			1L, "테스트 상품", List.of("image1.jpg"), LocalDateTime.now(), 5L);
	}

	@Nested
	@DisplayName("경매 상세 조회 테스트")
	class FindAuction {

		@DisplayName("경매 상세 조회 성공 - 진행중인 경매(레디스)")
		@Test
		void success_findAuction_redis() {
			// given
			Long auctionId = 1L;
			Long bidCount = 5L;

			given(auctionRedisService.findRedisAuctionVo(auctionId)).willReturn(auctionVo);
			given(auctionRedisService.calculateAuctionBidCount(auctionId)).willReturn(bidCount);
			given(bidRedisService.findWonBidVo(auctionId, auctionVo.getCurrentPrice())).willReturn(bidVo);

			// when
			FindDetailAuctionResponse response = auctionService.findAuction(auctionId);

			// then
			assertThat(response.auctionId()).isEqualTo(auctionId);
			assertThat(response.sellerNickname()).isEqualTo("판매자닉네임");
			assertThat(response.bidUserNickname()).isEqualTo("입찰자닉네임");
			assertThat(response.startPrice()).isEqualTo(100000L);
			assertThat(response.currentPrice()).isEqualTo(120000L);
			assertThat(response.productName()).isEqualTo("테스트 상품");
			assertThat(response.bidCount()).isEqualTo(bidCount);

			verify(auctionRedisService, times(1)).findRedisAuctionVo(auctionId);
			verify(auctionRedisService, times(1)).calculateAuctionBidCount(auctionId);
			verify(bidRedisService, times(1)).findWonBidVo(auctionId, auctionVo.getCurrentPrice());
		}

		@DisplayName("경매 상세 조회 성공 - 종료된 경매(RDB)")
		@Test
		void success_findAuction_rdb() {
			// given
			Long auctionId = 1L;

			given(auctionRedisService.findRedisAuctionVo(auctionId)).willReturn(null);
			given(bidDomainService.findWonBidByAuctionId(auctionId)).willReturn(bid);
			given(auctionDomainService.findAuctionDetailInfoByAuctionId(auctionId))
				.willReturn(auctionFindDetailInfo);

			// when
			FindDetailAuctionResponse response = auctionService.findAuction(auctionId);

			// then
			assertThat(response.auctionId()).isEqualTo(auctionId);
			assertThat(response.sellerNickname()).isEqualTo("판매자닉네임");
			assertThat(response.bidUserNickname()).isEqualTo("입찰자닉네임");
			assertThat(response.startPrice()).isEqualTo(100000L);
			assertThat(response.currentPrice()).isEqualTo(120000L);
			assertThat(response.productName()).isEqualTo("테스트 상품");
			assertThat(response.bidCount()).isEqualTo(5L);

			verify(auctionRedisService, times(1)).findRedisAuctionVo(auctionId);
			verify(bidDomainService, times(1)).findWonBidByAuctionId(auctionId);
			verify(auctionDomainService, times(1)).findAuctionDetailInfoByAuctionId(auctionId);
		}
	}
}
