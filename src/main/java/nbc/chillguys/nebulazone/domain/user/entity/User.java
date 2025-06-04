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

	@Column(unique = true, nullable = false)
	private String phone;

	@Column(unique = true, nullable = false)
	private String nickname;

	private String profileImage;

	private int point;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OAuthType oauthType;

	private Long oauthId;

	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@ElementCollection
	@CollectionTable(
		name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id")
	)
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
		int point, OAuthType oauthType, Long oauthId, String providerId, Set<UserRole> roles, Set<Address> addresses) {
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.point = point;
		this.oauthType = oauthType;
		this.oauthId = oauthId;
		this.providerId = providerId;
		this.roles = roles != null ? roles : new HashSet<>();
		this.addresses = addresses != null ? addresses : new HashSet<>();
		this.status = UserStatus.ACTIVE;
	}

	public void addRole(UserRole role) {
		this.roles.add(role);
	}

	public void withdraw() {
		this.status = UserStatus.INACTIVE;
		this.deletedAt = LocalDateTime.now();
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
}

