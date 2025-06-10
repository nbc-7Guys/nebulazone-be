package nbc.chillguys.nebulazone.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.domain.user.dto.AdminUserSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;

public interface AdminUserQueryRepository {

	Page<User> searchUsers(AdminUserSearchQueryCommand query, Pageable pageable);
}
