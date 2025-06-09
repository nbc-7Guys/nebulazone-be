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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
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
		String uploadUrl = generateUploadUrl(file);

		uploadFile(file, uploadUrl);

		return uploadUrl.split("\\?")[0];
	}

	public void generateDeleteUrlAndDeleteFile(String presignedUrl) {
		String presignedDeleteUrl = generateDeleteUrl(presignedUrl);

		deleteFile(presignedDeleteUrl);
	}

	public String generateDeleteUrl(String imageUrl) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(imageUrl.substring(imageUrl.lastIndexOf("/") + 1))
			.build();

		DeleteObjectPresignRequest deleteObjectPresignRequest = DeleteObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.deleteObjectRequest(deleteObjectRequest)
			.build();

		return s3Presigner.presignDeleteObject(deleteObjectPresignRequest).url().toString();
	}

	public void deleteFile(String deleteUrl) {
		ResponseEntity<String> response = restClient
			.delete()
			.uri(URI.create(deleteUrl))
			.retrieve()
			.toEntity(String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.error("S3 파일 삭제 실패: {}, body: {}", response.getStatusCode(), response.getBody());
		}
	}

	public String generateUploadUrl(MultipartFile multipartFile) {
		String fileName = getFileName(multipartFile);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(fileName)
			.contentType(multipartFile.getContentType())
			.contentLength(multipartFile.getSize())
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(10))
			.putObjectRequest(putObjectRequest)
			.build();

		PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
		return presignedPutObjectRequest.url().toString();
	}

	public void uploadFile(MultipartFile file, String uploadUrl) {
		try {
			ResponseEntity<String> response = restClient
				.put()
				.uri(URI.create(uploadUrl))
				.headers(httpHeaders -> {
					httpHeaders.setContentType(MediaType.valueOf(Objects.requireNonNull(file.getContentType())));
					httpHeaders.setContentLength(file.getSize());
				})
				.body(file.getBytes())
				.retrieve()
				.toEntity(String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("S3 업로드 실패: {}, body: {}", response.getStatusCode(), response.getBody());
			}
		} catch (IOException e) {
			log.error("파일 변환 실패", e);
		}
	}

	private String getFileName(MultipartFile file) {
		String extension = Objects.requireNonNull(file.getOriginalFilename())
			.substring(file.getOriginalFilename().lastIndexOf("."));
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid + extension;
	}
}
