package nbc.chillguys.nebulazone.domain.review.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nbc.chillguys.nebulazone.domain.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {
	Optional<Review> findByIdAndCatalogId(Long id, Long catalogId);
}
