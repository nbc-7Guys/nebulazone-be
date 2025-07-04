package nbc.chillguys.nebulazone.common.util;

import java.io.Serializable;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.SerializationUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

	public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return Optional.of(cookie);
				}
			}
		}

		return Optional.empty();
	}

	public static Cookie createCookie(String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(maxAge);
		cookie.setSecure(true);

		return cookie;
	}

	public static void deleteCookie(HttpServletResponse response, String name) {
		Cookie cookie = new Cookie(name, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		response.addCookie(cookie);
	}

	public static String serialize(Object object) {
		return Base64.getUrlEncoder()
			.encodeToString(SerializationUtils.serialize((Serializable)object));
	}

	public static <T> T deserialize(Cookie cookie, Class<T> cls) {
		return cls.cast(SerializationUtils.deserialize(
			Base64.getUrlDecoder().decode(cookie.getValue())));
	}
}
