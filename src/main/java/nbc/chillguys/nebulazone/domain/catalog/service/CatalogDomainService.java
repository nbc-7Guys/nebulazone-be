package nbc.chillguys.nebulazone.domain.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogSearchCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogErrorCode;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogException;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogEsRepository;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogRepository;
import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogDomainService {
	private final CatalogRepository catalogRepository;
	private final CatalogEsRepository catalogEsRepository;

	/**
	 * 카탈로그 검색
	 * @param command keyword, type, page, size
	 * @return 카탈로그 목록
	 * @author 이승현
	 */
	public Page<CatalogDocument> searchCatalog(CatalogSearchCommand command) {
		Pageable pageable = PageRequest.of(command.page() - 1, command.size());

		return catalogEsRepository.searchCatalog(command.keyword(), command.type(), pageable);
	}

	/**
	 * 카탈로그 상세 조회
	 * @param catalogId 카탈로그 id
	 * @return catalog
	 * @author 이승현
	 */
	public Catalog getCatalogById(Long catalogId) {
		return catalogRepository.findWithReviewById(catalogId)
			.orElseThrow(() -> new CatalogException(CatalogErrorCode.CATALOG_NOT_FOUND));
	}
}
