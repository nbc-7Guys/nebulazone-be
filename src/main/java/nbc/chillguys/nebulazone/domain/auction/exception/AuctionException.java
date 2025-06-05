package nbc.chillguys.nebulazone.domain.auction.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class AuctionException extends BaseException {
	private final AuctionErrorCode errorCode;
}
