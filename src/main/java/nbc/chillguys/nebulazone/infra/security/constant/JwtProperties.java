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

	public long getAccessTokenExpiredMillis() {
		return token.access().validity().toMillis();
	}

	public long getRefreshTokenExpiredMillis() {
		return token.refresh().validity().toMillis();
	}

	public Date getAccessTokenExpiredDate(Date now) {
		return new Date(now.getTime() + getAccessTokenExpiredMillis());
	}

	public Date getRefreshTokenExpiredDate(Date now) {
		return new Date(now.getTime() + getRefreshTokenExpiredMillis());
	}

	public long getAccessTokenValiditySeconds() {
		return token.access().validity().getSeconds();
	}

	public long getRefreshTokenValiditySeconds() {
		return token.refresh().validity().getSeconds();
	}
}
