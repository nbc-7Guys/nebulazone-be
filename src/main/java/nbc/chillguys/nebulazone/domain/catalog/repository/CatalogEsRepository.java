package nbc.chillguys.nebulazone.domain.catalog.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

public interface CatalogEsRepository extends ElasticsearchRepository<CatalogDocument, Long>, CustomCatalogEsRepository {
}
