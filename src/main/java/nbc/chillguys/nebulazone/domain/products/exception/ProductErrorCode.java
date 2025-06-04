package nbc.chillguys.nebulazone.domain.products.exception;

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
	NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "판매 상품 주인이 아닙니다.");

	private final HttpStatus status;
	private final String message;
}
