package nbc.chillguys.nebulazone.domain.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.catalog.dto.request.CatalogAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminInfo;
import nbc.chillguys.nebulazone.domain.catalog.dto.CatalogAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogErrorCode;
import nbc.chillguys.nebulazone.domain.catalog.exception.CatalogException;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogAdminRepositoryCustom;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogRepository;

@Service
@RequiredArgsConstructor
public class CatalogAdminDomainService {
	private final CatalogAdminRepositoryCustom catalogAdminRepositoryCustom;
	private final CatalogRepository catalogRepository;

	/**
	 * 어드민 카탈로그 목록을 조건에 맞게 페이징 조회합니다.<br>
	 * 검색어/타입 등 필터 조건을 적용하여 Catalog 엔티티를 AdminCatalogInfo로 변환합니다.
	 *
	 * @param command   도메인 계층 카탈로그 조회 조건 DTO
	 * @param pageable  페이징 정보
	 * @return AdminCatalogInfo 목록 Page
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<CatalogAdminInfo> findCatalogs(CatalogAdminSearchQueryCommand command, Pageable pageable) {
		return catalogAdminRepositoryCustom.searchCatalogs(command, pageable)
			.map(CatalogAdminInfo::from);
	}

	/**
	 * 카탈로그 정보를 수정합니다
	 *
	 * @param catalogId 수정할 카탈로그 ID
	 * @param request   수정 요청 DTO (이름, 설명, 타입)
	 * @throws CatalogException 카탈로그가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	@Transactional
	public void updateCatalog(Long catalogId, CatalogAdminUpdateRequest request) {

		Catalog catalog = findCatalogId(catalogId);

		catalog.update(request.name(), request.description(), request.type());
	}

	/**
	 * 카탈로그를 완전 삭제합니다.<br>
	 * (물리적으로 DB에서 제거)
	 *
	 * @param catalogId 삭제할 카탈로그 ID
	 * @throws CatalogException 카탈로그가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	@Transactional
	public void deleteCatalog(Long catalogId) {

		Catalog catalog = findCatalogId(catalogId);

		catalogRepository.delete(catalog);
	}

	/**
	 * 카탈로그 ID로 엔티티를 조회합니다.<br>
	 * 존재하지 않을 경우 CatalogException을 발생시킵니다.
	 *
	 * @param catalogId 조회할 카탈로그 ID
	 * @return Catalog 엔티티
	 * @throws CatalogException 카탈로그가 존재하지 않을 때 발생
	 * @author 정석현
	 */
	public Catalog findCatalogId(Long catalogId) {
		return catalogRepository.findById(catalogId)
			.orElseThrow(() -> new CatalogException(CatalogErrorCode.CATALOG_NOT_FOUND));
	}
}
