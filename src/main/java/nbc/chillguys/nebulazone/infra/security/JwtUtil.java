package nbc.chillguys.nebulazone.infra.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;
import nbc.chillguys.nebulazone.infra.security.constant.JwtConstants;
import nbc.chillguys.nebulazone.infra.security.constant.JwtProperties;
import nbc.chillguys.nebulazone.infra.security.dto.AuthTokens;
import nbc.chillguys.nebulazone.infra.security.exception.JwtTokenErrorCode;
import nbc.chillguys.nebulazone.infra.security.exception.JwtTokenException;
import nbc.chillguys.nebulazone.infra.security.filter.exception.JwtFilterErrorCode;
import nbc.chillguys.nebulazone.infra.security.filter.exception.JwtFilterException;

@Component
@RequiredArgsConstructor
public class JwtUtil {
	private final UserCacheService userCacheService;
	private final JwtProperties jwtProperties;

	@Value("${jwt.secret.key}")
	private String key;
	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(key.getBytes());
	}

	public String generateAccessToken(User user) {
		Date now = new Date();

		return Jwts.builder()
			.subject(user.getEmail())
			.id(user.getId().toString())
			.claim(JwtConstants.KEY_ROLES, user.getRoles())
			.issuedAt(now)
			.expiration(jwtProperties.getAccessTokenExpiredDate(now))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public String generateRefreshToken(User user) {
		Date now = new Date();

		return Jwts.builder()
			.subject(user.getEmail())
			.id(user.getId().toString())
			.claim(JwtConstants.KEY_ROLES, user.getRoles())
			.issuedAt(now)
			.expiration(jwtProperties.getRefreshTokenExpiredDate(now))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public AuthTokens generateTokens(User user) {
		String accessToken = generateAccessToken(user);
		String refreshToken = generateRefreshToken(user);

		return AuthTokens.of(accessToken, refreshToken, JwtConstants.TOKEN_PREFIX,
			jwtProperties.getAccessTokenValiditySeconds(), jwtProperties.getRefreshTokenValiditySeconds());
	}

	public AuthTokens regenerateAccessToken(String refreshToken) {
		if (isTokenExpired(refreshToken)) {
			throw new JwtTokenException(JwtTokenErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		User user = getUserFromToken(refreshToken);

		String accessToken = generateAccessToken(user);

		return AuthTokens.of(accessToken, null, JwtConstants.TOKEN_PREFIX,
			jwtProperties.getAccessTokenValiditySeconds(), null);
	}

	public boolean isTokenExpired(String token) {
		Claims claims = parseToken(token);
		return claims.getExpiration().before(new Date());
	}

	public User getUserFromToken(String token) {
		Claims claims = parseToken(token);

		Long userId = Long.valueOf(claims.getId());

		Date expiration = claims.getExpiration();

		long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

		return userCacheService.getUserById(userId, ttl);
	}

	public Claims parseToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException expiredJwtException) {
			throw new JwtFilterException(JwtFilterErrorCode.EXPIRED_JWT_TOKEN);
		} catch (MalformedJwtException malformedJwtException) {
			throw new JwtFilterException(JwtFilterErrorCode.NOT_VALID_JWT_TOKEN);
		} catch (SignatureException signatureException) {
			throw new JwtFilterException(JwtFilterErrorCode.NOT_VALID_SIGNATURE);
		} catch (UnsupportedJwtException unsupportedJwtException) {
			throw new JwtFilterException(JwtFilterErrorCode.NOT_VALID_CONTENT);
		} catch (Exception e) {
			throw new JwtFilterException(JwtFilterErrorCode.BAD_REQUEST);
		}
	}
}
