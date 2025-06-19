package nbc.chillguys.nebulazone.domain.pointhistory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.chillguys.nebulazone.domain.common.audit.BaseEntity;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_histories")
public class PointHistory extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "point_history_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Long price;

	private String account;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PointHistoryType pointHistoryType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PointHistoryStatus pointHistoryStatus;

	@Builder
	public PointHistory(
		Long price,
		String account,
		User user,
		PointHistoryType pointHistoryType,
		PointHistoryStatus pointHistoryStatus
	) {
		this.price = price;
		this.account = account;
		this.user = user;
		this.pointHistoryType = pointHistoryType;
		this.pointHistoryStatus = pointHistoryStatus;
	}

	public void approve() {
		this.pointHistoryStatus = PointHistoryStatus.ACCEPT;
	}

	public void reject() {
		this.pointHistoryStatus = PointHistoryStatus.REJECT;
	}
}
