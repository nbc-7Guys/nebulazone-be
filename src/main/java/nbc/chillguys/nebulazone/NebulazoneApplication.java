package nbc.chillguys.nebulazone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NebulazoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(NebulazoneApplication.class, args);
	}

}
