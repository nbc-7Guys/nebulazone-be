package nbc.chillguys.nebulazone.common.exception;

public abstract class BaseException extends RuntimeException {

	public abstract ErrorCode getErrorCode();
}
