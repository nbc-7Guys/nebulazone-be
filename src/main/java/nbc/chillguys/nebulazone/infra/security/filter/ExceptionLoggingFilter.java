package nbc.chillguys.nebulazone.infra.security.filter;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

@Component
public class ExceptionLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingFilter.class);
	private static final Set<String> EXCLUDED_PATHS = Set.of(
		"/actuator/prometheus", "/actuator/health", "/actuator/info", "/ws/**", "/ws", "/swagger-ui/**"
	);

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain)
		throws ServletException, IOException {
		String uri = request.getRequestURI();

		if (EXCLUDED_PATHS.stream().anyMatch(uri::startsWith)) {
			filterChain.doFilter(request, response);
			return;
		}

		String ip = getClientIP(request);
		String method = request.getMethod();
		String traceId = java.util.UUID.randomUUID().toString().replace("-", "");

		MDC.put("ip", ip);
		MDC.put("uri", uri);
		MDC.put("method", method);
		MDC.put("traceId", traceId);

		log.warn("[REQUEST] {} {} | IP={} | traceId={}", method, uri, ip, traceId);

		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			// 인증/인가/비즈니스 등 모든 예외 잡아서 로그로 남김
			log.error("[FILTER ERROR] {} {} | IP={} | traceId={} | message={}",
				method, uri, ip, traceId, e.getMessage(), e);
			throw e; // 꼭 다시 던져야 ExceptionHandler에서 처리됨
		} finally {
			// (옵션) 응답 로그
			log.warn("[RESPONSE] {} {} | status={} | IP={} | traceId={}", method, uri, response.getStatus(), ip,
				traceId);
			// MDC 초기화 (메모리릭 방지)
			MDC.clear();
		}
	}

	private String getClientIP(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0];
		}
		return ip;
	}
}
