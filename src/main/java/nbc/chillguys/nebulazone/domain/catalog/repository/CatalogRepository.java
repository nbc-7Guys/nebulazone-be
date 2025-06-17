package nbc.chillguys.nebulazone.domain.catalog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
	@EntityGraph(attributePaths = {"reviews"})
	Optional<Catalog> findWithReviewById(Long id);
}
