package nbc.chillguys.nebulazone.domain.product.service;

import java.util.List;

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
	private final ProductDomainService productDomainService;

	/**
	 * 검색 조건과 페이징 정보에 따라 상품 목록을 조회합니다.
	 *
	 * @param command  상품 검색 조건
	 * @param pageable 페이징 정보
	 * @return 상품 정보 페이지
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public Page<ProductAdminInfo> findProducts(ProductAdminSearchQueryCommand command, Pageable pageable) {
		Page<Product> page = productRepository.searchProducts(command, pageable);
		return page.map(ProductAdminInfo::from);
	}

	/**
	 * 단일 상품 정보를 상세 조회합니다.
	 *
	 * @param productId 조회할 상품 ID
	 * @return 상품 정보 DTO
	 * @author 정석현
	 */
	@Transactional(readOnly = true)
	public ProductAdminInfo getProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		return ProductAdminInfo.from(product);
	}

	/**
	 * 상품 정보를 수정합니다.<br>
	 * - 이름, 설명, 이미지, 거래방식, 가격 등 주요 정보 변경<br>
	 * - 수정 후 ES(검색엔진) 인덱스에 반영합니다.
	 *
	 * @param productId 수정할 상품 ID
	 * @param request   수정 요청 데이터
	 * @author 정석현
	 */
	@Transactional
	public Product updateProduct(Long productId, ProductAdminUpdateRequest request) {

		Product product = findByIdWithJoin(productId);

		product.update(
			request.name(),
			request.description()
		);

		if (request.txMethod() != null && request.txMethod() != product.getTxMethod()) {
			product.changeTxMethod(request.txMethod(), request.price());

		} else if (request.price() != null && !request.price().equals(product.getPrice())) {
			product.changePrice(request.price());
		}

		return product;
	}

	/**
	 * 상품을 삭제(소프트 딜리트) 처리합니다.<br>
	 * 삭제 후 ES(검색엔진) 인덱스에서도 제거합니다.
	 *
	 * @param productId 삭제할 상품 ID
	 * @author 정석현
	 */
	@Transactional
	public Long deleteProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		product.delete();
		return product.getId();
	}

	/**
	 * 삭제된 상품을 복원(undo delete)합니다.<br>
	 * 복원 후 ES(검색엔진) 인덱스에 반영합니다.
	 *
	 * @param productId 복원할 상품 ID
	 * @author 정석현
	 */
	public Product restoreProduct(Long productId) {
		Product product = findByIdWithJoin(productId);
		product.restore();
		return product;
	}

	/**
	 * 상품 ID로 연관 엔티티를 포함하여 상품을 조회합니다.<br>
	 * 존재하지 않을 경우 예외를 발생시킵니다.
	 *
	 * @param productId 상품 ID
	 * @return 상품 엔티티
	 * @author 정석현
	 */
	public Product findByIdWithJoin(Long productId) {
		return productRepository.findByIdWithJoin(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}

	/**
	 * 상품 이미지 업데이트
	 *
	 * @param product 대상 상품
	 * @param productImageUrs 수정할 Image 리스트
	 * @return 수정된 상품
	 * @author 전나겸
	 */
	public Product updateProductImages(Product product, List<String> productImageUrs) {
		product.updateProductImage(productImageUrs);
		return product;
	}
}
