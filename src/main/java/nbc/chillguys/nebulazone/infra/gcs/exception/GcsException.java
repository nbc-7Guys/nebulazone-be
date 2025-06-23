package nbc.chillguys.nebulazone.infra.gcs.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class GcsException extends BaseException {
	private final GcsErrorCode errorCode;
}
