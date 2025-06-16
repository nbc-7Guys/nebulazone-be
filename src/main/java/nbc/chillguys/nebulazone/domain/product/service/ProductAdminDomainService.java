package nbc.chillguys.nebulazone.domain.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.product.dto.request.ProductAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminInfo;
import nbc.chillguys.nebulazone.domain.product.dto.ProductAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.product.exception.ProductException;
import nbc.chillguys.nebulazone.domain.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductAdminDomainService {

	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public Page<ProductAdminInfo> findProducts(ProductAdminSearchQueryCommand command, Pageable pageable) {
		Page<Product> page = productRepository.searchProducts(command, pageable);
		return page.map(ProductAdminInfo::from);
	}

	@Transactional(readOnly = true)
	public ProductAdminInfo getProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		return ProductAdminInfo.from(product);
	}

	@Transactional
	public void updateProduct(Long productId, ProductAdminUpdateRequest request) {

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

	@Transactional
	public void deleteProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		product.delete();
	}

	public void restoreProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		product.restore();
	}

	public Product findByIdWithJoin(Long productId) {
		return productRepository.findByIdWithJoin(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}

}
