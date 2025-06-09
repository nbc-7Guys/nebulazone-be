package nbc.chillguys.nebulazone.infra.security.constant;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenExpiredConstant {
	private static final long MILLISECOND = 1000L;

	@Value("${jwt.token.access.second}")
	private long accessSecond;

	@Value("${jwt.token.access.minute}")
	private long accessMinute;

	@Value("${jwt.token.access.hour}")
	private long accessHour;

	@Value("${jwt.token.refresh.second}")
	private long refreshSecond;

	@Value("${jwt.token.refresh.minute}")
	private long refreshMinute;

	@Value("${jwt.token.refresh.hour}")
	private long refreshHour;

	public long getAccessTokenExpiredTime() {
		return accessHour * accessMinute * accessSecond * MILLISECOND;
	}

	public long getRefreshTokenExpiredTime() {
		return refreshHour * refreshMinute * refreshSecond * MILLISECOND;
	}

	public Date getAccessTokenExpiredDate(Date date) {
		return new Date(date.getTime() + getAccessTokenExpiredTime());
	}

	public Date getRefreshTokenExpiredDate(Date date) {
		return new Date(date.getTime() + getRefreshTokenExpiredTime());
	}
}
