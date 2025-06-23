package nbc.chillguys.nebulazone.infra.gcs.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsErrorCode;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsException;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GcsConfig {
	private final ResourceLoader resourceLoader;

	@Value("${gcp.credentials.location}")
	private String keyPath;

	@Bean
	public Storage storage() {
		Resource resource = resourceLoader.getResource(keyPath);

		if (!resource.exists()) {
			throw new GcsException(GcsErrorCode.CREDENTIALS_FILE_NOT_FOUND);
		}

		try (InputStream inputStream = resource.getInputStream()) {
			return StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(inputStream))
				.build()
				.getService();
		} catch (IOException e) {
			throw new GcsException(GcsErrorCode.CREDENTIALS_READ_ERROR);
		}
	}
}
