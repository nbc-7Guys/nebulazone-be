package nbc.chillguys.nebulazone.domain.post.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import nbc.chillguys.nebulazone.domain.post.vo.PostDocument;

public interface PostEsRepository extends ElasticsearchRepository<PostDocument, Long>, PostEsRepositoryCustom {

}
