package nbc.chillguys.nebulazone.domain.products.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDomainService {

	private final ProductRepository productRepository;

	@Transactional
	public Product createProduct(ProductCreateCommand command, List<String> productImageUrls) {
		Product product = Product.of(command.name(), command.description(), command.price(),
			command.txMethod(), command.endTime(), command.user(), command.catalog());

		Product saveProduct = productRepository.save(product);
		product.addProductImages(productImageUrls);

		return saveProduct;
	}
}
