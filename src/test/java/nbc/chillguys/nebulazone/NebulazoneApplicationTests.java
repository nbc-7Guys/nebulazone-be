package nbc.chillguys.nebulazone;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import nbc.chillguys.nebulazone.application.auction.service.AuctionSchedulerService;

@ActiveProfiles("test")
@SpringBootTest
class NebulazoneApplicationTests {

	@TestConfiguration
	static class MockConfig {
		@Bean
		public AuctionSchedulerService auctionSchedulerService() {
			return Mockito.mock(AuctionSchedulerService.class);
		}
	}

	@Test
	void contextLoads() {
	}

}
