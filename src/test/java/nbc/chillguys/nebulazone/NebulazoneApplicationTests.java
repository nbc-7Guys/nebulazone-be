package nbc.chillguys.nebulazone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.google.cloud.storage.Storage;

@ActiveProfiles("test")
@SpringBootTest
class NebulazoneApplicationTests {

	@MockitoBean
	private Storage storage;

	@Test
	void contextLoads() {
	}

}
