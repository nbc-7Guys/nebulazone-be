package nbc.chillguys.nebulazone.application.product.dto.request;

import java.util.List;

public record UpdateImagesProductRequest(
	List<String> remainImageUrls
) {
}
