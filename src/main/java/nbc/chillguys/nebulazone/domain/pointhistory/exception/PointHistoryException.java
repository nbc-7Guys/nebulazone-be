package nbc.chillguys.nebulazone.domain.pointhistory.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class PointHistoryException extends BaseException {
	private final PointHistoryErrorCode errorCode;
}
