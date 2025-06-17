package nbc.chillguys.nebulazone.common.util;

import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Slf4j
public class LogUtils {
	public static void logWarn(Throwable throwable) {
		if (throwable instanceof BaseException baseException) {
			log.warn("예외 발생: {} (ErrorCode: {})", baseException.getErrorCode().getMessage(),
				baseException.getErrorCode());
		} else {
			log.warn("예외 발생: {})", throwable.getMessage());
		}

		StackTraceElement[] stackTrace = throwable.getStackTrace();
		if (stackTrace.length > 0) {
			StackTraceElement first = stackTrace[0];
			log.warn("발생 위치: {}:{} - Thread: {}, Method: {}",
				first.getClassName(), first.getLineNumber(),
				Thread.currentThread().getName(), first.getMethodName());
		}
	}

	public static void logError(Throwable throwable) {
		log.error("예외 발생: {})", throwable.getMessage());

		log.warn("전체 예외 스택:", throwable);
	}
}
