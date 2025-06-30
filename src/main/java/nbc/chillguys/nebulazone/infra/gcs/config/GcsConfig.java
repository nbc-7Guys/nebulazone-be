package nbc.chillguys.nebulazone.infra.gcs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsErrorCode;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GcsConfig {

	@Bean
	public Storage storage() {
		try {
			return StorageOptions.getDefaultInstance().getService();
		} catch (Exception e) {
			log.error("GCP Storage 인증 실패: {}", e.getMessage());
			throw new GcsException(GcsErrorCode.CREDENTIALS_READ_ERROR);
		}
	}
}
