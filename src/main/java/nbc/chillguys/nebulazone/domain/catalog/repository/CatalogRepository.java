package nbc.chillguys.nebulazone.domain.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;

public interface CatalogRepository extends JpaRepository<Catalog, Long> {
}
