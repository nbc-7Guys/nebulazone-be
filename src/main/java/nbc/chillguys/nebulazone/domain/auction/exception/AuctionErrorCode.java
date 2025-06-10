package nbc.chillguys.nebulazone.domain.auction.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
	INVALID_AUCTION_SORT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 경매 정렬 타입 입니다."),
	ALREADY_DELETED_AUCTION(HttpStatus.BAD_REQUEST, "이미 삭제된 경매입니다."),
	ALREADY_WON_AUCTION(HttpStatus.BAD_REQUEST, "이미 낙찰된 경매 입니다."),
	ALREADY_CLOSED_AUCTION(HttpStatus.BAD_REQUEST, "이미 종료된 경매 입니다."),
	AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경매입니다."),
	AUCTION_NOT_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 경매만 삭제할 수 있습니다."),
	AUCTION_NOT_CLOSED(HttpStatus.CONFLICT, "경매가 종료되어야 삭제할 수 있습니다."),
	AUCTION_END_TIME_INVALID(HttpStatus.BAD_REQUEST, "경매 종료 시간이 현재 시간보다 늦어야 합니다.");

	private final HttpStatus status;
	private final String message;
}
