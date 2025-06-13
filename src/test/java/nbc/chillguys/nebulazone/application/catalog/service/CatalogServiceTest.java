package nbc.chillguys.nebulazone.application.catalog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.catalog.dto.response.CatalogResponse;
import nbc.chillguys.nebulazone.application.catalog.dto.response.SearchCatalogResponse;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogSearchCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.catalog.service.CatalogDomainService;
import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@DisplayName("카탈로그 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
	@Mock
	private CatalogDomainService catalogDomainService;

	@InjectMocks
	private CatalogService catalogService;

	@Spy
	private Catalog catalog;

	private CatalogDocument catalogDocument;

	@BeforeEach
	void init() {
		catalog = Catalog.builder()
			.name("test")
			.description("test_description")
			.type(CatalogType.GPU)
			.build();

		ReflectionTestUtils.setField(catalog, "id", 1L);

		catalogDocument = new CatalogDocument(catalog.getId(), catalog.getName(), catalog.getDescription(), null,
			CatalogType.GPU.name(), null, null, null, null);
	}

	@Nested
	@DisplayName("카탈로그 검색 테스트")
	class SearchCatalogTest {
		@Test
		@DisplayName("카탈로그 검색 성공")
		void success_searchCatalog() {
			// Given
			String keyword = "test";
			CatalogType type = CatalogType.GPU;
			int page = 0;
			int size = 10;
			CatalogSearchCommand command = CatalogSearchCommand.of(keyword, type, page, size);

			Page<CatalogDocument> catalogDocuments = new PageImpl<>(List.of(catalogDocument),
				PageRequest.of(page, size),
				1);

			given(catalogDomainService.searchCatalog(any()))
				.willReturn(catalogDocuments);

			// When
			Page<SearchCatalogResponse> result = catalogService.searchCatalog(keyword, type, page, size);

			// Then
			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().getFirst().catalogName()).isEqualTo("test");

			verify(catalogDomainService, times(1)).searchCatalog(command);
		}
	}

	@Nested
	@DisplayName("카탈로그 조회 테스트")
	class GetCatalogTest {
		@Test
		@DisplayName("카탈로그 조회 성공")
		void success_getCatalog() {
			// Given
			Long catalogId = 1L;

			given(catalogDomainService.getCatalogById(catalogId))
				.willReturn(catalog);

			// When
			CatalogResponse response = catalogService.getCatalog(catalogId);

			// Then
			assertThat(response.catalogId()).isEqualTo(catalogId);
			assertThat(response.catalogName()).isEqualTo("test");

			verify(catalogDomainService, times(1)).getCatalogById(catalogId);
		}
	}

}
