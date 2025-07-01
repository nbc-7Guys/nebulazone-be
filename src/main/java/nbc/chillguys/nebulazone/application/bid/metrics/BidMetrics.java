package nbc.chillguys.nebulazone.application.bid.metrics;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BidMetrics {

	private final MeterRegistry meterRegistry;

	public void countBidSuccess() {
		meterRegistry.counter("bid.success.count").increment();
	}

	public void recordBidLatency(long millis) {
		meterRegistry.timer("bid.process.latency").record(millis, TimeUnit.MILLISECONDS);
	}

	public void recordBidAmount(long amount) {
		DistributionSummary.builder("bid.amount.submitted")
			.register(meterRegistry)
			.record(amount);
	}

	public void countBidCancel() {
		meterRegistry.counter("bid.cancel.count").increment();
	}
}
