package nbc.chillguys.nebulazone.domain.bid.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum BidErrorCode implements ErrorCode {
	ALREADY_BID_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 입찰 입니다."),
	CANNOT_BID_OWN_AUCTION(HttpStatus.FORBIDDEN, "내 경매에는 입찰할 수 없습니다."),
	CANNOT_CANCEL_WON_BID(HttpStatus.BAD_REQUEST, "낙찰된 입찰은 취소할 수 없습니다."),
	BID_NOT_FOUND(HttpStatus.NOT_FOUND, "입찰 내역이 존재하지 않습니다."),
	BID_PRICE_TOO_LOW(HttpStatus.NOT_FOUND, "입찰 시 기존 입찰가보다 높아야 합니다."),
	BID_NOT_OWNER(HttpStatus.FORBIDDEN, "내 입찰 내역이 아닙니다."),
	BID_CANCEL_TIME_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "경매 종료 30분 전부터는 입찰을 취소할 수 없습니다."),
	BID_AUCTION_MISMATCH(HttpStatus.FORBIDDEN, "해당 경매의 입찰이 아닙니다.");

	private final HttpStatus status;
	private final String message;
}
