package nbc.chillguys.nebulazone.domain.chat.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
	CHAT_ROOM_NOT_FOUND(HttpStatus.BAD_REQUEST, "채팅방을 찾을 수 없습니다."),
	CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방에 접근할 권한이 없습니다."),

	PRODUCT_SOLD_OUT(HttpStatus.BAD_REQUEST, "판매 완료된 상품은 채팅이 불가능합니다."),
	CANNOT_CHAT_WITH_SELF(HttpStatus.FORBIDDEN, "구매자 본인의 상품 입니다."),

	CHAT_SEND_FAILED(HttpStatus.BAD_REQUEST, "메시지를 보내는데 실패하였습니다."),

	CHAT_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "채팅기록을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
