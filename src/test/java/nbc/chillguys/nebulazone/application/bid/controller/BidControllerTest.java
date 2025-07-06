package nbc.chillguys.nebulazone.application.bid.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.service.AuctionRedisService;
import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.DeleteBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindBidResponse;
import nbc.chillguys.nebulazone.application.bid.dto.response.FindMyBidsResponse;
import nbc.chillguys.nebulazone.application.bid.service.BidRedisService;
import nbc.chillguys.nebulazone.application.bid.service.BidService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.auction.service.AuctionDomainService;
import nbc.chillguys.nebulazone.domain.bid.service.BidDomainService;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@DisplayName("입찰 컨트롤러 단위 테스트")
@Import({TestSecurityConfig.class, TestMockConfig.class})
@WebMvcTest(
	controllers = BidController.class,
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = {
				JwtAuthenticationFilter.class
			})
	})
class BidControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private BidService bidService;

	@MockitoBean
	private BidDomainService bidDomainService;

	@MockitoBean
	private AuctionDomainService auctionDomainService;

	@MockitoBean
	private AuctionRedisService auctionRedisService;

	@MockitoBean
	private BidRedisService bidRedisService;

	@Nested
	@DisplayName("입찰 생성 테스트")
	class CreateBidTest {

		@DisplayName("입찰 생성 성공")
		@Test
		@WithCustomMockUser
		void success_createBid() throws Exception {
			// given
			Long auctionId = 1L;
			CreateBidRequest request = new CreateBidRequest(1000L);
			String requestBody = objectMapper.writeValueAsString(request);
			CreateBidResponse mockCreateBidResponse = new CreateBidResponse(auctionId, request.price(),
				LocalDateTime.now());
			given(bidRedisService.createBid(eq(auctionId), any(), eq(request.price()))).willReturn(
				mockCreateBidResponse);

			// when & then
			mockMvc.perform(
				post("/auctions/{auctionId}/bids", auctionId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody)
			)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.auctionId").value(auctionId))
				.andExpect(jsonPath("$.bidPrice").value(request.price()))
				.andDo(print());

			verify(bidRedisService, times(1)).createBid(eq(auctionId), any(), eq(request.price()));

		}

	}

	@Nested
	@DisplayName("특정 경매의 입찰 전체 조회 테스트")
	class FindBidsByAuctionIdTest {

		@DisplayName("특정 경매의 입찰 전체 조회 성공")
		@Test
		void success_findBidsByAuctionId() throws Exception {
			// given
			Long auctionId = 1L;
			int page = 0;
			int size = 10;

			FindBidResponse mockResponse = new FindBidResponse(
				100L, "testUser", "BID", 1000L, LocalDateTime.now(), auctionId
			);
			CommonPageResponse<FindBidResponse> expectedResponse = CommonPageResponse.from(
				new PageImpl<>(List.of(mockResponse), PageRequest.of(page, size), 1));
			given(bidService.findBidsByAuctionId(auctionId, page, size)).willReturn(expectedResponse);

			// when & then
			mockMvc.perform(
				get("/auctions/{auctionId}/bids", auctionId)
					.param("page", String.valueOf(page))
					.param("size", String.valueOf(size))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].bidUserId").value(100L))
				.andExpect(jsonPath("$.content[0].bidUserNickname").value("testUser"))
				.andExpect(jsonPath("$.content[0].bidStatus").value("BID"))
				.andExpect(jsonPath("$.content[0].bidPrice").value(1000L))
				.andDo(print());

			verify(bidService, times(1)).findBidsByAuctionId(auctionId, page, size);
		}

	}

	@Nested
	@DisplayName("내 입찰 전체 조회 테스트")
	class FindMyBidsTest {
		@DisplayName("내 입찰 전체 조회 성공")
		@Test
		@WithCustomMockUser
		void success_findMyBids() throws Exception {
			// given
			Long userId = 1L;
			int page = 0;
			int size = 10;

			FindMyBidsResponse mockResponse = new FindMyBidsResponse(
				100L, "testUser", "BID", 1000L, LocalDateTime.now(), 1L, 1L, "product1"
			);
			CommonPageResponse<FindMyBidsResponse> expectedResponse = CommonPageResponse.from(
				new PageImpl<>(List.of(mockResponse), PageRequest.of(page, size), 1));
			given(bidService.findMyBids(any(), eq(page), eq(size))).willReturn(expectedResponse);

			// when & then
			mockMvc.perform(
				get("/bids/me")
					.param("page", String.valueOf(page))
					.param("size", String.valueOf(size))
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].bidUserId").value(100L))
				.andExpect(jsonPath("$.content[0].bidUserNickname").value("testUser"))
				.andExpect(jsonPath("$.content[0].bidPrice").value(1000L))
				.andDo(print());

			verify(bidService, times(1)).findMyBids(any(), eq(page), eq(size));
		}
	}

	@Nested
	@DisplayName("입찰 취소 테스트")
	class CancelBid {
		@DisplayName("입찰 취소 성공")
		@Test
		@WithCustomMockUser
		void success_cancelBid() throws Exception {
			// given
			Long auctionId = 1L;
			Long bidPrice = 1000L;
			DeleteBidResponse expectedResponse = new DeleteBidResponse("test-uuid", 1000L, 1L, "CANCEL");
			given(bidRedisService.cancelBid(any(), eq(auctionId), eq(bidPrice))).willReturn(expectedResponse);

			// when & then
			mockMvc.perform(
				delete("/auctions/{auctionId}/bids/{bidPrice}", auctionId, bidPrice)
			)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bidUuid").value("test-uuid"))
				.andExpect(jsonPath("$.bidPrice").value(1000L))
				.andExpect(jsonPath("$.auctionId").value(auctionId))
				.andExpect(jsonPath("$.bidStatus").value("CANCEL"))
				.andDo(print());

			verify(bidRedisService, times(1)).cancelBid(any(), eq(auctionId), eq(bidPrice));
		}
	}
}
