package nbc.chillguys.nebulazone.domain.bid.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BidStatus {
	WON("낙찰"),
	BID("입찰"),
	CANCEL("입찰취소");

	private final String message;

}
