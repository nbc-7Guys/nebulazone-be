package nbc.chillguys.nebulazone.domain.catalog.repository;

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
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.catalog.vo.CatalogDocument;

@Repository
@RequiredArgsConstructor
public class CatalogEsRepositoryCustomImpl implements CatalogEsRepositoryCustom {
	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public Page<CatalogDocument> searchCatalog(String keyword, String type, Pageable pageable) {
		BoolQuery.Builder builder = QueryBuilders.bool()
			.must(m -> m.term(t -> t.field("type").value(type)));

		if (StringUtils.hasText(keyword)) {
			builder.must(m -> m
				.multiMatch(mm -> mm
					.query(keyword)
					.fields("name", "description")
					.operator(Operator.And)
					.type(TextQueryType.CrossFields)
				)
			);
		}

		Query query = Query.of(q -> q.bool(builder.build()));

		NativeQuery nativeQuery = NativeQuery.builder()
			.withQuery(query)
			.withPageable(pageable)
			.withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
			.build();

		SearchHits<CatalogDocument> hits = elasticsearchOperations.search(nativeQuery, CatalogDocument.class);

		return new PageImpl<>(
			hits.stream()
				.map(SearchHit::getContent)
				.toList(),
			pageable,
			hits.getTotalHits()
		);
	}
}
