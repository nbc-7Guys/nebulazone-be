package nbc.chillguys.nebulazone.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.user.dto.UserAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface UserAdminRepositoryCustom {

	Page<User> searchUsers(UserAdminSearchQueryCommand query, Pageable pageable);
}
