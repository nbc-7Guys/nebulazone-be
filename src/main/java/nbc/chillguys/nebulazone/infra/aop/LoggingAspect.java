package nbc.chillguys.nebulazone.infra.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void restController() {
	}

	@Before("restController()")
	public void logRequestInfo(JoinPoint joinPoint) {
		// 요청정보 가져오기
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attr == null) {
			return;
		}
		HttpServletRequest request = attr.getRequest();

		// 1. 엔드포인트가 /admin 으로 시작하는지 체크
		String uri = request.getRequestURI();
		if (!uri.startsWith("/admin")) {
			return;
		}

		// 2. 인증 정보 확인 (관리자만)
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!isAdmin(authentication)) {
			return; // 관리자가 아니면 로그 찍지 않음
		}

		String method = request.getMethod();
		String className = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();
		String userInfo = getUserInfo(authentication);

		log.info("[ADMIN API 요청] {} {} | Controller={}.{} | 유저={}",
			method, uri, className, methodName, userInfo);
	}

	private boolean isAdmin(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()
			&& !"anonymousUser".equals(authentication.getPrincipal())) {
			return authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		}
		return false;
	}

	private String getUserInfo(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()
			&& !"anonymousUser".equals(authentication.getPrincipal())) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof User user) {
				return String.format("id=%d, email=%s", user.getId(), user.getEmail());
			} else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
				return userDetails.getUsername();
			}
			return principal.toString();
		}
		return "비회원";
	}
}
