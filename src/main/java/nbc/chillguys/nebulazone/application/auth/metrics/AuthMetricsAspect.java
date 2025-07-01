package nbc.chillguys.nebulazone.application.auth.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuthMetricsAspect {

	private final AuthMetrics authMetrics;

	@Around("@annotation(nbc.chillguys.nebulazone.application.auth.metrics.TrackAuthMetrics)")
	public Object trackAuth(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			Object result = pjp.proceed();
			authMetrics.countSuccess();
			return result;
		} catch (Exception e) {
			authMetrics.countFailure();
			throw e;
		} finally {
			long elapsed = System.currentTimeMillis() - start;
			authMetrics.recordLatency(elapsed);
		}
	}
}
