package nbc.chillguys.nebulazone.application.auction.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.EndAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindSortTypeAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.auction.service.AuctionService;
import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.infra.redis.dto.FindAllAuctionsDto;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@DisplayName("경매 컨트롤러 단위 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(
	controllers = AuctionController.class,
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = {
				JwtAuthenticationFilter.class
			})
	}
)
class AuctionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuctionService auctionService;

	@MockitoBean
	private AuctionRedisService auctionRedisService;

	@Nested
	@DisplayName("정렬 기준 경매 리스트 조회 테스트")
	class FindAuctionSortTypeTest {

		@DisplayName("조회 성공 - 마감 임박순 경매, 5개")
		@Test
		void success_findAuctionSortType_closing() throws Exception {
			// given
			List<FindAllAuctionsDto> mockAuctions = List.of(
				new FindAllAuctionsDto(1L, 100000L, 120000L, false,
					LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L, "경매상품1", "image1.jpg", 3L),
				new FindAllAuctionsDto(2L, 200000L, 220000L, false,
					LocalDateTime.now().plusDays(2), LocalDateTime.now(), 2L, "경매상품2", "image2.jpg", 5L),
				new FindAllAuctionsDto(3L, 150000L, 170000L, false,
					LocalDateTime.now().plusDays(3), LocalDateTime.now(), 3L, "경매상품3", "image3.jpg", 2L),
				new FindAllAuctionsDto(4L, 300000L, 350000L, false,
					LocalDateTime.now().plusDays(4), LocalDateTime.now(), 4L, "경매상품4", "image4.jpg", 7L),
				new FindAllAuctionsDto(5L, 250000L, 280000L, false,
					LocalDateTime.now().plusDays(5), LocalDateTime.now(), 5L, "경매상품5", "image5.jpg", 4L)
			);
			FindSortTypeAuctionResponse response = FindSortTypeAuctionResponse.from(mockAuctions);

			given(auctionRedisService.findAuctionsBySortType(AuctionSortType.CLOSING))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/auctions/sorted")
					.param("sort", "closing")
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctions").isArray())
				.andExpect(jsonPath("$.auctions.length()").value(5))
				.andExpect(jsonPath("$.auctions[0].auctionId").value(1L))
				.andExpect(jsonPath("$.auctions[0].startPrice").value(100000L))
				.andExpect(jsonPath("$.auctions[0].currentPrice").value(120000L))
				.andExpect(jsonPath("$.auctions[0].productName").value("경매상품1"))
				.andExpect(jsonPath("$.auctions[0].productImageUrl").value("image1.jpg"))
				.andExpect(jsonPath("$.auctions[0].bidCount").value(3L))
				.andExpect(jsonPath("$.lastUpdated").isNotEmpty());

			verify(auctionRedisService, times(1)).findAuctionsBySortType(AuctionSortType.CLOSING);
		}

		@DisplayName("조회 성공 - 인기순(입찰개수) 경매, 5개")
		@Test
		void success_findAuctionSortType_popular() throws Exception {
			// given
			List<FindAllAuctionsDto> mockAuctions = List.of(
				new FindAllAuctionsDto(1L, 100000L, 120000L, false,
					LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L, "인기경매1", "image1.jpg", 15L),
				new FindAllAuctionsDto(2L, 200000L, 220000L, false,
					LocalDateTime.now().plusDays(2), LocalDateTime.now(), 2L, "인기경매2", "image2.jpg", 12L),
				new FindAllAuctionsDto(3L, 150000L, 170000L, false,
					LocalDateTime.now().plusDays(3), LocalDateTime.now(), 3L, "인기경매3", "image3.jpg", 10L),
				new FindAllAuctionsDto(4L, 300000L, 350000L, false,
					LocalDateTime.now().plusDays(4), LocalDateTime.now(), 4L, "인기경매4", "image4.jpg", 8L),
				new FindAllAuctionsDto(5L, 250000L, 280000L, false,
					LocalDateTime.now().plusDays(5), LocalDateTime.now(), 5L, "인기경매5", "image5.jpg", 6L)
			);
			FindSortTypeAuctionResponse response = FindSortTypeAuctionResponse.from(mockAuctions);

			given(auctionRedisService.findAuctionsBySortType(AuctionSortType.POPULAR))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/auctions/sorted")
					.param("sort", "popular")
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctions").isArray())
				.andExpect(jsonPath("$.auctions.length()").value(5))
				.andExpect(jsonPath("$.auctions[0].auctionId").value(1L))
				.andExpect(jsonPath("$.auctions[0].startPrice").value(100000L))
				.andExpect(jsonPath("$.auctions[0].currentPrice").value(120000L))
				.andExpect(jsonPath("$.auctions[0].productName").value("인기경매1"))
				.andExpect(jsonPath("$.auctions[0].productImageUrl").value("image1.jpg"))
				.andExpect(jsonPath("$.auctions[0].bidCount").value(15L))
				.andExpect(jsonPath("$.lastUpdated").isNotEmpty());

			verify(auctionRedisService, times(1)).findAuctionsBySortType(AuctionSortType.POPULAR);
		}
	}

	@Nested
	@DisplayName("경매 상세 조회 테스트")
	class FindAuctionDetailTest {

		@DisplayName("경매 상세 조회 성공")
		@Test
		void success_findAuction() throws Exception {
			// given
			Long auctionId = 1L;
			FindDetailAuctionResponse response = new FindDetailAuctionResponse(
				auctionId, 1L, "판매자닉네임", "seller@test.com",
				2L, "입찰자닉네임", "bidder@test.com",
				100000L, 120000L, false, LocalDateTime.now().plusDays(1),
				1L, "테스트 상품", List.of("image1.jpg"), LocalDateTime.now(), 5L);

			given(auctionService.findAuction(auctionId)).willReturn(response);

			// when & then
			mockMvc.perform(get("/auctions/{auctionId}", auctionId)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctionId").value(auctionId))
				.andExpect(jsonPath("$.sellerNickname").value("판매자닉네임"))
				.andExpect(jsonPath("$.bidUserNickname").value("입찰자닉네임"))
				.andExpect(jsonPath("$.startPrice").value(100000L))
				.andExpect(jsonPath("$.currentPrice").value(120000L))
				.andExpect(jsonPath("$.productName").value("테스트 상품"));

			verify(auctionService, times(1)).findAuction(auctionId);
		}
	}

	@Nested
	@DisplayName("경매 수동 낙찰 테스트")
	class ManualEndAuctionTest {

		@DisplayName("경매 수동 낙찰 성공")
		@Test
		@WithCustomMockUser
		void success_manualEndAuction() throws Exception {
			// given
			Long auctionId = 1L;
			ManualEndAuctionRequest request = new ManualEndAuctionRequest(120000L, "낙찰자닉네임");
			EndAuctionResponse response = new EndAuctionResponse(
				auctionId, 1L, "낙찰자닉네임", "bidder@test.com",
				120000L, "테스트 상품", LocalDateTime.now());

			given(auctionRedisService.manualEndAuction(eq(auctionId), any(), eq(request)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/auctions/{auctionId}", auctionId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctionId").value(auctionId))
				.andExpect(jsonPath("$.wonProductName").value("테스트 상품"))
				.andExpect(jsonPath("$.wonBidPrice").value(120000L))
				.andExpect(jsonPath("$.winnerNickname").value("낙찰자닉네임"));

			verify(auctionRedisService, times(1)).manualEndAuction(eq(auctionId), any(), eq(request));
		}
	}

	@Nested
	@DisplayName("경매 삭제 테스트")
	class DeleteAuctionTest {

		@DisplayName("경매 삭제 성공")
		@Test
		@WithCustomMockUser
		void success_deleteAuction() throws Exception {
			// given
			Long auctionId = 1L;
			DeleteAuctionResponse response = DeleteAuctionResponse.of(auctionId, 1L);

			given(auctionRedisService.deleteAuction(eq(auctionId), any()))
				.willReturn(response);

			// when & then
			mockMvc.perform(delete("/auctions/{auctionId}", auctionId)
					.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auctionId").value(auctionId))
				.andExpect(jsonPath("$.productId").value(1L));

			verify(auctionRedisService, times(1)).deleteAuction(eq(auctionId), any());
		}
	}
}
