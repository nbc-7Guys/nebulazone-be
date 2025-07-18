package nbc.chillguys.nebulazone.application.product.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.product.dto.request.CreateProductRequest;
import nbc.chillguys.nebulazone.application.product.dto.response.ProductResponse;
import nbc.chillguys.nebulazone.application.product.dto.response.SearchProductResponse;
import nbc.chillguys.nebulazone.application.product.service.ProductService;
import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.product.entity.ProductEndTime;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@Import({TestSecurityConfig.class, TestMockConfig.class})
@DisplayName("상품 컨트롤러 단위 테스트")
@WebMvcTest(
	controllers = ProductController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			JwtAuthenticationFilter.class
		})
	}
)
class ProductControllerTest {
	@MockitoBean
	private ProductService productService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Nested
	@DisplayName("상품 생성")
	class CreateProductTest {

		@Test
		@DisplayName("상품 생성 성공 - 경매")
		@WithCustomMockUser
		void success_createProduct_auction() throws Exception {
			// given
			Long catalogId = 1L;
			CreateProductRequest request = new CreateProductRequest(
				"testProduct",
				"testDescription",
				"auction",
				1000L,
				"hour_24");

			ProductResponse response = new ProductResponse(
				1L, "testProduct", "testDescription", 1000L,
				ProductTxMethod.AUCTION, false, ProductEndTime.HOUR_24,
				LocalDateTime.now(), LocalDateTime.now(), List.of());

			given(productService.createProduct(any(), eq(catalogId), any(CreateProductRequest.class)))
				.willReturn(response);

			// when
			ResultActions perform = mockMvc.perform(
				post("/catalogs/{catalogId}/products", catalogId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)));

			// then
			perform.andDo(print())
				.andExpectAll(
					status().isCreated(),
					jsonPath("$.productId").value(1L),
					jsonPath("$.productName").value("testProduct"),
					jsonPath("$.productDescription").value("testDescription"),
					jsonPath("$.productPrice").value(1000L),
					jsonPath("$.productTxMethod").value("AUCTION"));
		}

		@Test
		@DisplayName("상품 생성 성공 - 즉시거래")
		@WithCustomMockUser
		void success_createProduct_direct() throws Exception {
			// given
			Long catalogId = 1L;
			CreateProductRequest request = new CreateProductRequest(
				"directProduct",
				"directDescription",
				"direct",
				2000L,
				null);

			ProductResponse response = new ProductResponse(
				2L, "directProduct", "directDescription", 2000L,
				ProductTxMethod.DIRECT, false, null,
				LocalDateTime.now(), LocalDateTime.now(), List.of());

			given(productService.createProduct(any(), eq(catalogId), any(CreateProductRequest.class)))
				.willReturn(response);

			// when
			ResultActions perform = mockMvc.perform(
				post("/catalogs/{catalogId}/products", catalogId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)));

			// then
			perform.andDo(print())
				.andExpectAll(
					status().isCreated(),
					jsonPath("$.productId").value(2L),
					jsonPath("$.productName").value("directProduct"),
					jsonPath("$.productDescription").value("directDescription"),
					jsonPath("$.productPrice").value(2000L),
					jsonPath("$.productTxMethod").value("DIRECT"));
		}
	}

	@Nested
	@DisplayName("상품 검색")
	class SearchProductTest {

		@Test
		@DisplayName("상품 검색 성공")
		void success_searchProduct() throws Exception {
			// Given
			SearchProductResponse response = new SearchProductResponse(1L, "testProduct",
				1L, 1L, "testSellerNickname", null, false, 1000L,
				ProductTxMethod.DIRECT.name(), LocalDateTime.now(), List.of());
			Page<SearchProductResponse> page = new PageImpl<>(List.of(response),
				PageRequest.of(0, 10), 1);

			given(productService.searchProduct(anyString(), anyString(), any(), any(), any(), anyInt(), anyInt()))
				.willReturn(page);

			// When
			ResultActions perform = mockMvc.perform(get("/products")
				.param("productname", "testProduct")
				.param("sellernickname", "testSellerNickname")
				.param("type", ProductTxMethod.DIRECT.name())
				.param("from", "1000")
				.param("to", "2000")
				.param("page", "1")
				.param("size", "10")
			);

			// Then
			perform.andDo(print())
				.andExpectAll(
					status().isOk(),
					jsonPath("$.content[0].productId")
						.value(1L),
					jsonPath("$.content[0].productName")
						.value("testProduct"),
					jsonPath("$.content[0].sellerNickname")
						.value("testSellerNickname"),
					jsonPath("$.content[0].productPrice")
						.value(1000L),
					jsonPath("$.content[0].txMethod")
						.value(ProductTxMethod.DIRECT.name()),
					jsonPath("$.page")
						.value(1),
					jsonPath("$.totalElements")
						.value(1)
				);
		}
	}

	@Nested
	@DisplayName("상품 조회")
	class GetProductTest {

		@Test
		@DisplayName("상품 조회 성공")
		void success_getProduct() throws Exception {
			// Given
			Long catalogId = 1L;
			Long productId = 1L;
			ProductResponse response = new ProductResponse(productId, "testProduct", "testDescription",
				1000L, ProductTxMethod.DIRECT, false, ProductEndTime.HOUR_12, LocalDateTime.now(),
				LocalDateTime.now(),
				List.of());

			given(productService.getProduct(catalogId, productId)).willReturn(response);

			// When & Then
			ResultActions perform = mockMvc.perform(
				get("/catalogs/{catalogId}/products/{productId}", catalogId, productId));

			perform.andDo(print())
				.andExpectAll(
					status().isOk(),
					jsonPath("$.productId")
						.value(productId),
					jsonPath("$.productName")
						.value("testProduct"),
					jsonPath("$.productPrice")
						.value(1000L),
					jsonPath("$.productTxMethod")
						.value(ProductTxMethod.DIRECT.name())
				);
		}
	}

}
