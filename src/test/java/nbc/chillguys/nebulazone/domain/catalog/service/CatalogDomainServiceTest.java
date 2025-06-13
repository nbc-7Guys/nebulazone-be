package nbc.chillguys.nebulazone.domain.catalog.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

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

import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogSearchCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogErrorCode;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogException;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogEsRepository;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogRepository;
import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@DisplayName("카탈로그 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CatalogDomainServiceTest {
	@Mock
	private CatalogRepository catalogRepository;

	@Mock
	private CatalogEsRepository catalogEsRepository;

	@InjectMocks
	private CatalogDomainService catalogDomainService;

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
		@DisplayName("카탈로그 검색 성공 - 모든 조건")
		void success_searchCatalog_allParameters() {
			// Given
			CatalogSearchCommand command = CatalogSearchCommand.of("test", CatalogType.GPU, 1, 10);

			given(catalogEsRepository.searchCatalog(anyString(), anyString(), any()))
				.willReturn(new PageImpl<>(List.of(catalogDocument), PageRequest.of(0, 10), 1));

			// When
			Page<CatalogDocument> catalogDocuments = catalogDomainService.searchCatalog(command);

			// Then
			verify(catalogEsRepository, times(1))
				.searchCatalog("test", CatalogType.GPU.name(), PageRequest.of(0, 10));
			assertThat(catalogDocuments.getContent().size())
				.isEqualTo(1);
			assertThat(catalogDocuments.getTotalElements())
				.isEqualTo(1);
			assertThat(catalogDocuments.getContent().getFirst().name().contains("test")
				|| catalogDocuments.getContent().getFirst().description().contains("test")).isTrue();

		}

		@Test
		@DisplayName("카탈로그 검색 성공 - 카탈로그 유형만 검색")
		void success_searchCatalog_noParameters() {
			CatalogSearchCommand command = CatalogSearchCommand.of(null, CatalogType.GPU, 1, 10);

			given(catalogEsRepository.searchCatalog(any(), anyString(), any()))
				.willReturn(new PageImpl<>(List.of(catalogDocument, catalogDocument), PageRequest.of(0, 10), 2));

			// When
			Page<CatalogDocument> catalogDocuments = catalogDomainService.searchCatalog(command);

			// Then
			verify(catalogEsRepository, times(1))
				.searchCatalog(null, CatalogType.GPU.name(), PageRequest.of(0, 10));
			assertThat(catalogDocuments.getContent().size())
				.isEqualTo(2);
			assertThat(catalogDocuments.getTotalElements())
				.isEqualTo(2);

		}
	}

	@Nested
	@DisplayName("카탈로그 조회 테스트")
	class GetCatalogTest {
		@Test
		@DisplayName("카탈로그 조회 성공")
		void success_getCatalogById() {
			// Given
			Long catalogId = 1L;

			Catalog mockCatalog = mock(Catalog.class);
			given(catalogRepository.findById(anyLong()))
				.willReturn(Optional.ofNullable(mockCatalog));

			// When
			Catalog result = catalogDomainService.getCatalogById(catalogId);

			// Then
			assertEquals(mockCatalog, result);

			verify(catalogRepository, times(1)).findById(catalogId);

		}

		@Test
		@DisplayName("카탈로그 조회 실패 - 카탈로그가 존재 하지 않음")
		void fail_getCatalogById_catalogNotFound() {
			// Given
			Long postId = 2L;

			given(catalogRepository.findById(anyLong()))
				.willReturn(Optional.empty());

			// When
			CatalogException exception = assertThrows(CatalogException.class,
				() -> catalogDomainService.getCatalogById(postId));

			// Then
			assertEquals(CatalogErrorCode.CATALOG_NOT_FOUND, exception.getErrorCode());

			verify(catalogRepository, times(1)).findById(postId);
			verifyNoMoreInteractions(catalogRepository);
		}
	}

}
