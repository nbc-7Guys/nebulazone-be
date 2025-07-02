package nbc.chillguys.nebulazone.application.auction.dto.request;

import jakarta.validation.constraints.NotNull;

public record ManualEndAuctionRequest(
	@NotNull(message = "낙찰 대상인 입찰 가격은 필수 입니다.")
	Long bidPrice,

	@NotNull(message = "낙찰 대상인 입찰 닉네임 필수 입니다.")
	String bidUserNickname

) {
}
