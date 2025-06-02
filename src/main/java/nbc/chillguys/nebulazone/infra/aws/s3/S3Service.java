package nbc.chillguys.nebulazone.infra.aws.s3;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {
	private final S3Presigner s3Presigner;
	private final RestClient restClient;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	public String generateUploadUrlAndUploadFile(MultipartFile file) {
		if (file.isEmpty()) {
			return null;
		}

		String fileName = getFileName(file);

		String contentType = file.getContentType();

		String presignedUrl = generateUploadUrl(fileName, contentType, file.getSize());

		if (!uploadFile(file, presignedUrl, contentType)) {
			return null;
		}

		return presignedUrl.split("\\?")[0];
	}

	public String generateUploadUrl(String fileName, String contentType, long contentLength) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(fileName)
			.contentType(contentType)
			.contentLength(contentLength)
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(putObjectRequest)
			.build();

		PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
		return presignedPutObjectRequest.url().toString();
	}

	private boolean uploadFile(MultipartFile file, String presignedUrl, String contentType) {
		try {
			ResponseEntity<String> response = restClient
				.put()
				.uri(URI.create(presignedUrl))
				.headers(httpHeaders -> {
					httpHeaders.setContentType(MediaType.valueOf(contentType));
					httpHeaders.setContentLength(file.getSize());
				})
				.body(file.getBytes())
				.retrieve()
				.toEntity(String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("S3 업로드 실패: {}, body: {}", response.getStatusCode(), response.getBody());
				return false;
			}
		} catch (IOException e) {
			log.error("파일 변환 실패", e);
			return false;
		}

		return true;
	}

	private String getFileName(MultipartFile file) {
		String extension = Objects.requireNonNull(file.getOriginalFilename())
			.substring(file.getOriginalFilename().lastIndexOf("."));
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid + extension;
	}
}
