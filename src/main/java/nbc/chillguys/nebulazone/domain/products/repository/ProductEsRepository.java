package nbc.chillguys.nebulazone.domain.products.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import nbc.chillguys.nebulazone.domain.products.vo.ProductDocument;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, Long>, CustomProductEsRepository {
}
