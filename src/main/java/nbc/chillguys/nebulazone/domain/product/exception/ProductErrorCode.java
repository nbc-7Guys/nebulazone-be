package nbc.chillguys.nebulazone.domain.product.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
	INVALID_PRODUCT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 판매상품 타입 입니다."),
	INVALID_END_TIME(HttpStatus.BAD_REQUEST, "유효하지 않은 종료 시간입니다."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 판매 상품입니다."),
	NOT_BELONGS_TO_CATALOG(HttpStatus.BAD_REQUEST, "판매 상품 지정한 카탈로그에 존재하지 않습니다."),
	NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "판매 상품 주인이 아닙니다."),
	ALREADY_AUCTION_TYPE(HttpStatus.CONFLICT, "이미 경매 방식 판매이므로 변경할 수 없습니다."),
	ALREADY_SOLD(HttpStatus.BAD_REQUEST, "이미 판매된 상품입니다."),
	AUCTION_PRODUCT_NOT_PURCHASABLE(HttpStatus.BAD_REQUEST, "옥션 상품은 구매할 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
