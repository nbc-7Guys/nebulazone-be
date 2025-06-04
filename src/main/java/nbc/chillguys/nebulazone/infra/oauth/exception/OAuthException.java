package nbc.chillguys.nebulazone.infra.oauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.BaseException;

@Getter
@RequiredArgsConstructor
public class OAuthException extends BaseException {
	private final OAuthErrorCode errorCode;
}
