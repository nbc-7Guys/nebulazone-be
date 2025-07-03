package nbc.chillguys.nebulazone.support.mock;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import nbc.chillguys.nebulazone.application.ban.service.BanService;
import nbc.chillguys.nebulazone.domain.ban.exception.BanErrorCode;
import nbc.chillguys.nebulazone.domain.ban.exception.BanException;

@TestConfiguration
public class TestMockConfig {

	@Bean
	public BanService banService() {
		BanService mock = Mockito.mock(BanService.class);

		Mockito.doThrow(new BanException(BanErrorCode.BAN_NOT_FOUND))
			.when(mock).validateBanned(Mockito.anyString());
		return mock;
	}
}
