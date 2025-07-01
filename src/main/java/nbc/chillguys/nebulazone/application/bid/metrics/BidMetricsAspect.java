package nbc.chillguys.nebulazone.application.bid.metrics;

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
public class BidMetricsAspect {

	private final BidMetrics bidMetrics;

	@Around("@annotation(nbc.chillguys.nebulazone.application.bid.metrics.TrackBidMetrics)")
	public Object track(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			Object result = pjp.proceed();
			bidMetrics.countBidSuccess();

			// 파라미터에서 입찰 금액 자동 추출
			Object[] args = pjp.getArgs();
			if (args.length >= 3 && args[2] instanceof Long bidPrice) {
				bidMetrics.recordBidAmount(bidPrice);
			}
			return result;
		} catch (Exception e) {
			// 실패 metric 필요시 여기에
			throw e;
		} finally {
			long elapsed = System.currentTimeMillis() - start;
			bidMetrics.recordBidLatency(elapsed);
		}
	}
}
