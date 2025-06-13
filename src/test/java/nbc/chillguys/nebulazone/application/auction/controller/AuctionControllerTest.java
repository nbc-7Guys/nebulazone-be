package nbc.chillguys.nebulazone.application.auction.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.auction.dto.request.ManualEndAuctionRequest;
import nbc.chillguys.nebulazone.application.auction.dto.response.DeleteAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindAllAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.FindDetailAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.dto.response.ManualEndAuctionResponse;
import nbc.chillguys.nebulazone.application.auction.service.AuctionService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.auction.entity.AuctionSortType;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@DisplayName("경매 컨트롤러 단위 테스트")
@WebMvcTest(AuctionController.class)
@Import(TestSecurityConfig.class)
class AuctionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuctionService auctionService;

    private static final Long AUCTION_ID = 1L;
    private static final Long BID_ID = 100L;
    private static final Long PRODUCT_ID = 50L;
    private static final Long USER_ID = 10L;
    private static final Long SELLER_ID = 20L;
    private static final Long WINNER_ID = 30L;
    private static final String PRODUCT_NAME = "테스트 CPU";
    private static final String PRODUCT_IMAGE_URL = "http://example.com/image.jpg";
    private static final String SELLER_NICKNAME = "판매자닉네임";
    private static final String SELLER_EMAIL = "seller@test.com";
    private static final String WINNER_NICKNAME = "입찰자닉네임";
    private static final String WINNER_EMAIL = "winner@test.com";
    private static final Long START_PRICE = 100000L;
    private static final Long CURRENT_PRICE = 150000L;
    private static final Long WON_PRICE = 200000L;
    private static final Long BID_COUNT = 5L;

    private final LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
    private final LocalDateTime wonDate = LocalDateTime.of(2024, 12, 30, 15, 30, 0);
    private final LocalDateTime productCreatedAt = LocalDateTime.of(2024, 12, 1, 10, 0, 0);

    @Nested
    @DisplayName("경매 전체 조회")
    class FindAuctionsTest {

        @Test
        @DisplayName("경매 전체 조회 성공 - 기본 페이징")
        @WithCustomMockUser
        void success_findAuctions() throws Exception {
            // given
            FindAllAuctionResponse auctionResponse = new FindAllAuctionResponse(
                AUCTION_ID, START_PRICE, CURRENT_PRICE, false,
                endTime, PRODUCT_NAME, PRODUCT_IMAGE_URL, BID_COUNT
            );
            List<FindAllAuctionResponse> content = List.of(auctionResponse);

            CommonPageResponse<FindAllAuctionResponse> response = CommonPageResponse.<FindAllAuctionResponse>builder()
                .content(content)
                .page(1)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

            given(auctionService.findAuctions(0, 20)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions"))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.content").isArray(),
                    jsonPath("$.content.length()").value(1),
                    jsonPath("$.content[0].auctionId").value(AUCTION_ID),
                    jsonPath("$.content[0].startPrice").value(START_PRICE),
                    jsonPath("$.content[0].currentPrice").value(CURRENT_PRICE),
                    jsonPath("$.content[0].productName").value(PRODUCT_NAME),
                    jsonPath("$.page").value(1),
                    jsonPath("$.size").value(20)
                );

            verify(auctionService).findAuctions(0, 20);
        }

        @Test
        @DisplayName("경매 전체 조회 성공 - 사용자 정의 페이징")
        @WithCustomMockUser
        void success_findAuctions_customPaging() throws Exception {
            // given
            CommonPageResponse<FindAllAuctionResponse> response = CommonPageResponse.<FindAllAuctionResponse>builder()
                .content(List.of())
                .page(2)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();

            given(auctionService.findAuctions(1, 10)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions")
                    .param("page", "2")
                    .param("size", "10"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.page").value(2),
                    jsonPath("$.size").value(10),
                    jsonPath("$.totalElements").value(0),
                    jsonPath("$.content").isEmpty()
                );

            verify(auctionService).findAuctions(1, 10);
        }

        @Test
        @DisplayName("경매 전체 조회 성공 - 페이지 0 이하 처리")
        @WithCustomMockUser
        void success_findAuctions_pageZeroOrBelow() throws Exception {
            // given
            FindAllAuctionResponse auctionResponse = new FindAllAuctionResponse(
                AUCTION_ID, START_PRICE, CURRENT_PRICE, false,
                endTime, PRODUCT_NAME, PRODUCT_IMAGE_URL, BID_COUNT
            );

            CommonPageResponse<FindAllAuctionResponse> response = CommonPageResponse.<FindAllAuctionResponse>builder()
                .content(List.of(auctionResponse))
                .page(1)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

            given(auctionService.findAuctions(0, 20)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.page").value(1),
                    jsonPath("$.content.length()").value(1)
                );

            verify(auctionService).findAuctions(0, 20);
        }

        @Test
        @DisplayName("경매 전체 조회 성공 - 기본값 적용")
        @WithCustomMockUser
        void success_findAuctions_defaultValues() throws Exception {
            // given
            CommonPageResponse<FindAllAuctionResponse> response = CommonPageResponse.<FindAllAuctionResponse>builder()
                .content(List.of())
                .page(1)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

            given(auctionService.findAuctions(0, 20)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.page").value(1),
                    jsonPath("$.size").value(20)
                );

            verify(auctionService).findAuctions(0, 20);
        }
    }

    @Nested
    @DisplayName("정렬된 경매 조회")
    class FindSortedAuctionsTest {

        @Test
        @DisplayName("인기순 경매 조회 성공")
        @WithCustomMockUser
        void success_findAuctions_popular() throws Exception {
            // given
            FindAllAuctionResponse auctionResponse = new FindAllAuctionResponse(
                AUCTION_ID, START_PRICE, CURRENT_PRICE, false,
                endTime, PRODUCT_NAME, PRODUCT_IMAGE_URL, BID_COUNT
            );
            List<FindAllAuctionResponse> response = List.of(auctionResponse);

            given(auctionService.findAuctionsBySortType(AuctionSortType.POPULAR)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions/sorted")
                    .param("sort", "POPULAR"))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(1),
                    jsonPath("$[0].auctionId").value(AUCTION_ID),
                    jsonPath("$[0].startPrice").value(START_PRICE),
                    jsonPath("$[0].currentPrice").value(CURRENT_PRICE),
                    jsonPath("$[0].productName").value(PRODUCT_NAME),
                    jsonPath("$[0].bidCount").value(BID_COUNT)
                );

            verify(auctionService).findAuctionsBySortType(AuctionSortType.POPULAR);
        }

        @Test
        @DisplayName("마감임박순 경매 조회 성공")
        @WithCustomMockUser
        void success_findAuctions_closing() throws Exception {
            // given
            FindAllAuctionResponse auctionResponse = new FindAllAuctionResponse(
                AUCTION_ID, START_PRICE, CURRENT_PRICE, false,
                endTime, PRODUCT_NAME, PRODUCT_IMAGE_URL, BID_COUNT
            );
            List<FindAllAuctionResponse> response = List.of(auctionResponse);

            given(auctionService.findAuctionsBySortType(AuctionSortType.CLOSING)).willReturn(response);

            // when & then
            mockMvc.perform(get("/auctions/sorted")
                    .param("sort", "CLOSING"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$[0].auctionId").value(AUCTION_ID),
                    jsonPath("$[0].endTime").value(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                );

            verify().findAuctionsBySortType(AuctionSortType.CLOSING);
        }

        @Test
        @DisplayName("정렬된 경매 조회 - 빈 결과")
        @WithCustomMockUser
        void success_findAuctions_emptyResult() throws Exception {
            // given
            given(auctionService.findAuctionsBySortType(AuctionSortType.POPULAR)).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/auctions/sorted")
                    .param("sort", "POPULAR"))
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$").isArray(),
                    jsonPath("$.length()").value(0)
                );

        }
    }

    @Nested
    @DisplayName("경매 상세 조회")
    class FindAuctionDetailTest {

        @Test
        @DisplayName("경매 상세 조회 성공")
        @WithCustomMockUser
        void success_findAuction() throws Exception {
            // given
            FindDetailAuctionResponse detailResponse = new FindDetailAuctionResponse(
                AUCTION_ID, SELLER_ID, SELLER_NICKNAME, SELLER_EMAIL,
                USER_ID, WINNER_NICKNAME, WINNER_EMAIL,
                START_PRICE, CURRENT_PRICE, false, endTime,
                PRODUCT_ID, PRODUCT_NAME, PRODUCT_IMAGE_URL, productCreatedAt, BID_COUNT
            );

            given(auctionService.findAuction(AUCTION_ID)).willReturn(detailResponse);

            // when & then
            mockMvc.perform(get("/auctions/{auctionId}", AUCTION_ID))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.auctionId").value(AUCTION_ID),
                    jsonPath("$.sellerId").value(SELLER_ID),
                    jsonPath("$.sellerNickname").value(SELLER_NICKNAME),
                    jsonPath("$.sellerEmail").value(SELLER_EMAIL),
                    jsonPath("$.bidUserId").value(USER_ID),
                    jsonPath("$.bidUserNickname").value(WINNER_NICKNAME),
                    jsonPath("$.bidUserEmail").value(WINNER_EMAIL),
                    jsonPath("$.startPrice").value(START_PRICE),
                    jsonPath("$.currentPrice").value(CURRENT_PRICE),
                    jsonPath("$.isWon").value(false),
                    jsonPath("$.endTime").value(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                    jsonPath("$.productId").value(PRODUCT_ID),
                    jsonPath("$.productName").value(PRODUCT_NAME),
                    jsonPath("$.productImageUrl").value(PRODUCT_IMAGE_URL),
                    jsonPath("$.productCreatedAt").value(productCreatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                    jsonPath("$.bidCount").value(BID_COUNT)
                );

        }
    }

    @Nested
    @DisplayName("수동 낙찰")
    class ManualEndAuctionTest {

        @Test
        @DisplayName("수동 낙찰 성공")
        @WithCustomMockUser
        void success_manualEndAuction() throws Exception {
            // given
            ManualEndAuctionRequest request = new ManualEndAuctionRequest(BID_ID, PRODUCT_ID);
            ManualEndAuctionResponse manualEndResponse = new ManualEndAuctionResponse(
                AUCTION_ID, BID_ID, WINNER_ID, WINNER_NICKNAME, WINNER_EMAIL,
                WON_PRICE, PRODUCT_NAME, wonDate
            );

            given(auctionService.manualEndAuction(eq(AUCTION_ID), any(AuthUser.class), eq(request)))
                .willReturn(manualEndResponse);

            // when & then
            mockMvc.perform(post("/auctions/{auctionId}", AUCTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.auctionId").value(AUCTION_ID),
                    jsonPath("$.bidId").value(BID_ID),
                    jsonPath("$.winnerId").value(WINNER_ID),
                    jsonPath("$.winnerNickname").value(WINNER_NICKNAME),
                    jsonPath("$.winnerEmail").value(WINNER_EMAIL),
                    jsonPath("$.wonProductPrice").value(WON_PRICE),
                    jsonPath("$.wonProductName").value(PRODUCT_NAME),
                    jsonPath("$.wonDate").value(wonDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                );
		}
    }

    @Nested
    @DisplayName("경매 삭제")
    class DeleteAuctionTest {

        @Test
        @DisplayName("경매 삭제 성공")
        @WithCustomMockUser
        void success_deleteAuction() throws Exception {
            // given
            DeleteAuctionResponse deleteResponse = new DeleteAuctionResponse(AUCTION_ID);
            given(auctionService.deleteAuction(eq(AUCTION_ID), any(AuthUser.class))).willReturn(deleteResponse);

            // when & then
            mockMvc.perform(delete("/auctions/{auctionId}", AUCTION_ID))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_JSON),
                    jsonPath("$.auctionId").value(AUCTION_ID)
                );

        }
    }
}
