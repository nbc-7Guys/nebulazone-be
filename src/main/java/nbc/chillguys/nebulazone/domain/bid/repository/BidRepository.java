package nbc.chillguys.nebulazone.domain.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.bid.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, Long>, BidCustomRepository {

}
