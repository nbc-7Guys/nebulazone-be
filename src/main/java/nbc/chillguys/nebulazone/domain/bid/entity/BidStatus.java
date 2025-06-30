package nbc.chillguys.nebulazone.domain.bid.entity;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nbc.chillguys.nebulazone.domain.bid.exception.BidErrorCode;
import nbc.chillguys.nebulazone.domain.bid.exception.BidException;

@Getter
@AllArgsConstructor
public enum BidStatus {
	WON("낙찰"),
	BID("입찰"),
	CANCEL("입찰취소");

	private final String message;

	public static BidStatus of(String bidStatus) {
		return Arrays.stream(BidStatus.values())
			.filter(type -> type.name().equalsIgnoreCase(bidStatus))
			.findFirst()
			.orElseThrow(() -> new BidException(BidErrorCode.INVALID_BID_STATUS));
	}
}
