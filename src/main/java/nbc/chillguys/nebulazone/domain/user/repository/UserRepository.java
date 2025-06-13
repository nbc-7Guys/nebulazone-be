package nbc.chillguys.nebulazone.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository, AdminUserQueryRepository {
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}
