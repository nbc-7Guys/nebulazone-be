package nbc.chillguys.nebulazone.domain.common.validator.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageFileValidator implements ConstraintValidator<ImageFile, Object> {
	private final Tika tika = new Tika();
	private static final List<String> VALID_TYPE_LIST = List.of(
		MediaType.IMAGE_JPEG_VALUE,
		"image/pjpeg",
		MediaType.IMAGE_PNG_VALUE,
		MediaType.IMAGE_GIF_VALUE,
		"image/bmp",
		"image/x-windows-bmp"
	);
	private static final long MAX_SIZE = 2 * 1024 * 1024;

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}

		if (value instanceof MultipartFile file) {
			return isImage(file);
		}

		if (value instanceof List<?> list) {
			if (list.isEmpty()) {
				return false;
			}

			for (Object element : list) {
				if (!(element instanceof MultipartFile file) || !isImage(file)) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private boolean isImage(MultipartFile file) {
		if (file.getSize() > MAX_SIZE) {
			return false;
		}

		try (InputStream inputStream = file.getInputStream()) {
			String mimeType = tika.detect(inputStream);

			return VALID_TYPE_LIST.stream().anyMatch(validType -> validType.equalsIgnoreCase(mimeType));
		} catch (IOException e) {
			return false;
		}
	}
}
