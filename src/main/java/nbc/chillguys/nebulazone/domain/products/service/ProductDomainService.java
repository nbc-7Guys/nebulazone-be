package nbc.chillguys.nebulazone.domain.products.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductPurchaseCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.products.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.products.exception.ProductException;
import nbc.chillguys.nebulazone.domain.products.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDomainService {

	private final ProductRepository productRepository;

	/**
	 * 판매 상품 생성
	 * @param command 유저, 카탈로그, 상품 이름, 상품 가격, 상품 설명, 상품 거래 종류, 경매 종료 시간
	 * @param productImageUrls 상품 이미지 리스트
	 * @return Product
	 * @author 전나겸
	 */
	@Transactional
	public Product createProduct(ProductCreateCommand command, List<String> productImageUrls) {

		Product product = Product.builder()
			.seller(command.user())
			.catalog(command.catalog())
			.name(command.name())
			.description(command.description())
			.price(command.price())
			.txMethod(command.txMethod())
			.build();

		product.addProductImages(productImageUrls);

		return productRepository.save(product);
	}

	/**
	 * 삭제되지 않은 판매 상품 조회
	 * @param productId 상품 id
	 * @return product
	 * @author 윤정환
	 */
	public Product findActiveProductById(Long productId) {
		return productRepository.findByIdAndDeletedFalse(productId)
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}

	/**
	 * 판매되지 않은 일반 판매 상품 조회
	 * @param productId 상품 id
	 * @return product
	 * @author 윤정환
	 */
	public Product findAvailableProductById(Long productId) {
		Product product = findActiveProductById(productId);

		product.validateNotSold();
		product.validatePurchasable();

		return product;
	}

	/**
	 * 판매 상품 수정
	 * @param command 판매 상품 수정 정보
	 * @return product
	 * @author 윤정환
	 */
	@Transactional
	public Product updateProduct(ProductUpdateCommand command) {
		Product product = findActiveProductById(command.productId());

		product.validateBelongsToCatalog(command.catalog().getId());
		product.validateProductOwner(command.user().getId());

		product.update(command.name(), command.description(), command.imageUrls());

		// todo: 수정된 상품명 ES에 갱신

		return product;
	}

	/**
	 * 판매 방식 옥션으로 변경
	 * @param command 핀메 싱픔 정보
	 * @return product
	 * @author 윤정환
	 */
	@Transactional
	public Product changeToAuctionType(ChangeToAuctionTypeCommand command) {
		Product product = findActiveProductById(command.productId());

		if (Objects.equals(product.getTxMethod(), ProductTxMethod.AUCTION)) {
			throw new ProductException(ProductErrorCode.ALREADY_AUCTION_TYPE);
		}

		product.changeToAuctionType(command.price());

		return product;
	}

	/**
	 * 판매 상품 삭제
	 * @param command 판매 상품 삭제 정보
	 * @author 윤정환
	 */
	@Transactional
	public void deleteProduct(ProductDeleteCommand command) {
		Product product = findActiveProductById(command.productId());

		product.validateBelongsToCatalog(command.catalog().getId());
		product.validateProductOwner(command.user().getId());

		product.delete();

		// todo: 삭제된 정보 ES에 갱신
	}

	@Transactional
	public void purchaseProduct(ProductPurchaseCommand command) {
		Product product = findAvailableProductById(command.productId());

		product.purchase();
	}
}
