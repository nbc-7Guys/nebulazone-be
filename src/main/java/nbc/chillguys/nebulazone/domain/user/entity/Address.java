package nbc.chillguys.nebulazone.domain.user.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {
	private String roadAddress;

	private String detailAddress;
}
