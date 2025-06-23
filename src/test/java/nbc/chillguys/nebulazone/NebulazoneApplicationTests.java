package nbc.chillguys.nebulazone;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.cloud.storage.Storage;

import nbc.chillguys.nebulazone.application.auction.service.AuctionSchedulerService;

@ActiveProfiles("test")
@SpringBootTest
class NebulazoneApplicationTests {

	@MockitoBean
	private Storage storage;

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
