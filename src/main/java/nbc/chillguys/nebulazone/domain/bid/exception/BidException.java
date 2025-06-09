package nbc.chillguys.nebulazone.domain.bid.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class BidException extends BaseException {
	private final BidErrorCode errorCode;
}
