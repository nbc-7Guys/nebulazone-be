package nbc.chillguys.nebulazone.domain.products.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.products.dto.request.AdminProductUpdateRequest;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductInfo;
import nbc.chillguys.nebulazone.domain.products.dto.AdminProductSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class AdminProductDomainService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public Page<AdminProductInfo> findProducts(AdminProductSearchQueryCommand command, Pageable pageable) {
		Page<Product> page = productRepository.searchProducts(command, pageable);
		return page.map(AdminProductInfo::from);
	}

	@Transactional(readOnly = true)
	public AdminProductInfo getProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		return AdminProductInfo.from(product);
	}

	@Transactional
	public void updateProduct(Long productId, AdminProductUpdateRequest request) {

		Product product = findByIdWithJoin(productId);

		product.update(
			request.name(),
			request.description(),
			request.productImageUrls()
		);
		// 거래방식/가격 등도 필요시 엔티티에 추가 메서드로 변경
		if (request.txMethod() != null && request.txMethod() != product.getTxMethod()) {
			product.changeTxMethod(request.txMethod(), request.price());
		} else if (request.price() != null && !request.price().equals(product.getPrice())) {
			product.changePrice(request.price());
		}
	}

	public Product findByIdWithJoin(Long productId) {
		return productRepository.findByIdWithJoin(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}

}
