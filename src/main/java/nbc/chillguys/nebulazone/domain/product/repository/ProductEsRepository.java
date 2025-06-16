package nbc.chillguys.nebulazone.domain.product.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;

public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, Long>, ProductEsRepositoryCustom {
}
