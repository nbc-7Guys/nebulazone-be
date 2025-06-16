package nbc.chillguys.nebulazone.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;

@Repository
@RequiredArgsConstructor
public class ProductEsRepositoryCustomImpl implements ProductEsRepositoryCustom {
	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public Page<ProductDocument> searchProduct(String productName, String txMethod, Long priceFrom, Long priceTo,
		Pageable pageable) {
		BoolQuery.Builder builder = QueryBuilders.bool()
			.must(m -> m.term(t -> t.field("txMethod").value(txMethod)));

		if (StringUtils.hasText(productName)) {
			builder.must(m -> m.match(t -> t.field("name").query(productName)));
		}

		if (priceFrom != null && priceTo != null) {
			builder.must(m -> m.range(r -> r
				.untyped(u -> u.field("price")
					.gte(JsonData.of(priceFrom))
					.lte(JsonData.of(priceTo)))));
		} else if (priceFrom != null) {
			builder.must(m -> m.range(r -> r
				.untyped(u -> u.field("price")
					.gte(JsonData.of(priceFrom)))));
		} else if (priceTo != null) {
			builder.must(m -> m.range(r -> r
				.untyped(u -> u.field("price")
					.lte(JsonData.of(priceTo)))));
		}

		Query query = Query.of(q -> q.bool(builder.build()));

		NativeQuery nativeQuery = NativeQuery.builder()
			.withQuery(query)
			.withPageable(pageable)
			.withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
			.build();

		SearchHits<ProductDocument> hits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

		return new PageImpl<>(
			hits.stream()
				.map(SearchHit::getContent)
				.toList(),
			pageable,
			hits.getTotalHits()
		);
	}
}
