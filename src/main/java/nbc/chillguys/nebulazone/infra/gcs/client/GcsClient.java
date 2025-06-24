package nbc.chillguys.nebulazone.infra.gcs.client;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsErrorCode;
import nbc.chillguys.nebulazone.infra.gcs.exception.GcsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsClient {
	private final Storage storage;

	@Value("${gcp.storage.bucket}")
	private String bucketName;

	public String uploadFile(MultipartFile file) {
		String fileName = getFileName(file);

		try {
			BlobId blobId = BlobId.of(bucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
			storage.create(blobInfo, file.getBytes());

		} catch (IOException e) {
			throw new GcsException(GcsErrorCode.FILE_UPLOAD_FAILED);
		}

		return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
	}

	public void deleteFile(String imageUrl) {
		String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

		storage.delete(BlobId.of(bucketName, fileName));
	}

	private String getFileName(MultipartFile file) {
		String extension = Objects.requireNonNull(file.getOriginalFilename())
			.substring(file.getOriginalFilename().lastIndexOf("."));
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid + extension;
	}
}
