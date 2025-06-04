package nbc.chillguys.nebulazone.domain.user.repository;

import java.util.Optional;

import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface UserCustomRepository {
	Optional<User> findActiveUserByEmail(String email);

	Optional<User> findActiveUserById(Long userId);

	Optional<User> findActiveUserByEmailAndOAuthType(String email, OAuthType oAuthType);
}
