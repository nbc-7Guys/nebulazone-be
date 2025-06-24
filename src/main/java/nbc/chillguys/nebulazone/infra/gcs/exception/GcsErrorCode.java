package nbc.chillguys.nebulazone.infra.gcs.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GcsErrorCode implements ErrorCode {
	CREDENTIALS_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "GCS 자격 증명 파일을 찾을 수 없습니다"),
	CREDENTIALS_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GCS 자격 증명 파일 읽기 실패"),
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GCS 파일 업로드 실패");

	private final HttpStatus status;
	private final String message;
}
