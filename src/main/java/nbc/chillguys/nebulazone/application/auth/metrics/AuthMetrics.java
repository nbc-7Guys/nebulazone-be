package nbc.chillguys.nebulazone.application.auth.metrics;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthMetrics {

	private final MeterRegistry meterRegistry;

	public void recordLatency(long millis) {
		meterRegistry.timer("user.login.latency").record(millis, TimeUnit.MILLISECONDS);
	}

	public void countSuccess() {
		meterRegistry.counter("user.login.success.count").increment();
	}

	public void countFailure() {
		meterRegistry.counter("user.login.fail.count").increment();
	}
}
