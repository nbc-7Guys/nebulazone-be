package nbc.chillguys.nebulazone.domain.user.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.user.exception.UserErrorCode;
import nbc.chillguys.nebulazone.domain.user.exception.UserException;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	private String password;

	@Column(unique = true)
	private String phone;

	@Column(unique = true, nullable = false)
	private String nickname;

	private String profileImage;

	private Long point;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OAuthType oAuthType;

	private String oAuthId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@ElementCollection
	@CollectionTable(
		name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id")
	)
	@Enumerated(EnumType.STRING)
	private Set<UserRole> roles;

	@ElementCollection
	@CollectionTable(
		name = "user_addresses",
		joinColumns = @JoinColumn(name = "user_id")
	)
	private Set<Address> addresses;

	private LocalDateTime deletedAt;

	@Builder
	public User(String email, String password, String phone, String nickname, String profileImage,
		long point, OAuthType oAuthType, String oAuthId, Set<UserRole> roles, Set<Address> addresses) {
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.point = point;
		this.oAuthType = oAuthType;
		this.oAuthId = oAuthId;
		this.roles = roles != null ? roles : new HashSet<>();
		this.addresses = addresses != null ? addresses : new HashSet<>();
		this.status = UserStatus.ACTIVE;
	}

	public void withdraw() {
		this.status = UserStatus.INACTIVE;
		this.deletedAt = LocalDateTime.now();
	}

	public void updateEmail(String email) {
		this.email = email;
	}

	public void updatePhone(String phone) {
		this.phone = phone;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void usePoint(long usePoint) {
		if (this.point < usePoint) {
			throw new UserException(UserErrorCode.INSUFFICIENT_BALANCE);
		}

		this.point -= point;
	}

	public boolean hasNotEnoughPoint(int price) {
		return this.point < price;
	}

	public void changeStatus(UserStatus status) {
		if (status == UserStatus.INACTIVE) {
			this.status = UserStatus.INACTIVE;
			this.deletedAt = LocalDateTime.now();
		} else if (status == UserStatus.ACTIVE) {
			this.status = UserStatus.ACTIVE;
			this.deletedAt = null;
		}
	}

	public void updateRoles(Set<UserRole> roles) {
		if (roles == null || roles.isEmpty()) {
			throw new UserException(UserErrorCode.WRONG_ROLES);
		}
		this.roles.clear();
		this.roles.addAll(roles);
	}

}

