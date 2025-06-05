package nbc.chillguys.nebulazone.domain.auction.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
	INVALID_AUCTION_SORT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 경매 정렬 타입 입니다."),
	;

	private final HttpStatus status;
	private final String message;
}
