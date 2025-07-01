package nbc.chillguys.nebulazone.infra.security.constant;

import java.time.Duration;
import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	Token token
) {

	public record Token(
		Access access,
		Refresh refresh
	) {
	}

	public record Access(
		Duration validity
	) {
	}

	public record Refresh(
		Duration validity
	) {
	}

	private long getAccessTokenExpiredMillis() {
		return token.access().validity().toMillis();
	}

	private long getRefreshTokenExpiredMillis() {
		return token.refresh().validity().toMillis();
	}

	public Date getAccessTokenExpiredDate(Date now) {
		return new Date(now.getTime() + getAccessTokenExpiredMillis());
	}

	public Date getRefreshTokenExpiredDate(Date now) {
		return new Date(now.getTime() + getRefreshTokenExpiredMillis());
	}
}
