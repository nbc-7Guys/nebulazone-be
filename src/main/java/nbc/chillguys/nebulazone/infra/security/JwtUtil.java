package nbc.chillguys.nebulazone.infra.security;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.security.constant.JwtConstants;
import nbc.chillguys.nebulazone.infra.security.constant.TokenExpiredConstant;
import nbc.chillguys.nebulazone.infra.security.exception.JwtTokenErrorCode;
import nbc.chillguys.nebulazone.infra.security.exception.JwtTokenException;

@Component
public class JwtUtil {
	private final SecretKey secretKey;
	private final TokenExpiredConstant tokenExpiredConstant;

	public JwtUtil(@Value("${jwt.secret.key}") String secretKey, TokenExpiredConstant tokenExpiredConstant) {
		this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
		this.tokenExpiredConstant = tokenExpiredConstant;
	}

	public String generateAccessToken(AuthUser authUser) {
		Date now = new Date();

		return Jwts.builder()
			.subject(authUser.getEmail())
			.id(authUser.getId().toString())
			.claim(JwtConstants.KEY_ROLES, authUser.getRoles())
			.issuedAt(now)
			.expiration(tokenExpiredConstant.getAccessTokenExpiredDate(now))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public String generateRefreshToken(AuthUser authUser) {
		Date now = new Date();

		return Jwts.builder()
			.subject(authUser.getEmail())
			.id(authUser.getId().toString())
			.claim(JwtConstants.KEY_ROLES, authUser.getRoles())
			.issuedAt(now)
			.expiration(tokenExpiredConstant.getRefreshTokenExpiredDate(now))
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public String regenerateAccessToken(String refreshToken) {
		if (isTokenExpired(refreshToken)) {
			throw new JwtTokenException(JwtTokenErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		AuthUser authUser = getAuthUserFromToken(refreshToken);

		return generateAccessToken(authUser);
	}

	public boolean isTokenExpired(String token) {
		Claims claims = parseToken(token);
		return claims.getExpiration().before(new Date());
	}

	public AuthUser getAuthUserFromToken(String token) {
		Claims claims = parseToken(token);

		List<?> objects = claims.get("roles", List.class);
		Set<UserRole> roles = objects.stream()
			.map(String::valueOf)
			.map(UserRole::from)
			.collect(Collectors.toSet());

		return AuthUser.builder()
			.id(Long.valueOf(claims.getId()))
			.email(claims.getSubject())
			.roles(roles)
			.build();
	}

	public Claims parseToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException expiredJwtException) {
			throw new JwtTokenException(JwtTokenErrorCode.EXPIRED_JWT_TOKEN);
		} catch (MalformedJwtException malformedJwtException) {
			throw new JwtTokenException(JwtTokenErrorCode.NOT_VALID_JWT_TOKEN);
		} catch (SignatureException signatureException) {
			throw new JwtTokenException(JwtTokenErrorCode.NOT_VALID_SIGNATURE);
		} catch (UnsupportedJwtException unsupportedJwtException) {
			throw new JwtTokenException(JwtTokenErrorCode.NOT_VALID_CONTENT);
		} catch (Exception e) {
			throw new JwtTokenException(JwtTokenErrorCode.BAD_REQUEST);
		}
	}
}
