package nbc.chillguys.nebulazone.domain.user.entity;

import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Address {
	private String roadAddress;

	private String detailAddress;

	private String addressNickname;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Address address = (Address)object;
		return Objects.equals(roadAddress, address.roadAddress) && Objects.equals(detailAddress, address.detailAddress)
			&& Objects.equals(addressNickname, address.addressNickname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roadAddress, detailAddress, addressNickname);
	}

}
