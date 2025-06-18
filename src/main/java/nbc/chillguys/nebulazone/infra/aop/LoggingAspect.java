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
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void restController() {
	}

	@Before("restController()")
	public void logRequestInfo(JoinPoint joinPoint) {
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attr == null) {
			return;
		}
		HttpServletRequest request = attr.getRequest();

		String method = request.getMethod();
		String uri = request.getRequestURI();

		// 컨트롤러/메서드 정보
		String className = joinPoint.getSignature().getDeclaringTypeName();
		String methodName = joinPoint.getSignature().getName();

		// 유저 정보
		String userInfo = getUserInfo();

		log.info("[API 요청] {} | Controller={}.{} | 유저={}",
			method, className, methodName, userInfo);
	}

	private String getUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
			&& !"anonymousUser".equals(authentication.getPrincipal())) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof AuthUser authUser) {
				return String.format("id=%d, email=%s", authUser.getId(), authUser.getEmail());
			} else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
				return userDetails.getUsername();
			}
			return principal.toString();
		}
		return "비회원";
	}
}
