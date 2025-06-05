package nbc.chillguys.nebulazone.domain.products.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.products.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.products.dto.ProductDeleteCommand;
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

	@Transactional
	public Product createProduct(ProductCreateCommand command, List<String> productImageUrls) {
		Product product = Product.of(command.name(), command.description(), command.price(),
			command.txMethod(), command.user(), command.catalog());

		Product saveProduct = productRepository.save(product);
		product.addProductImages(productImageUrls);

		return saveProduct;
	}

	/**
	 * 판매 상품 수정
	 * @param command 판매 상품 수정 정보
	 * @return product
	 * @author 윤정환
	 */
	@Transactional
	public Product updateProduct(ProductUpdateCommand command) {
		// todo: 판매 상품 상세 조회 메서드 추가되면 교체
		Product product = productRepository.findById(command.productId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		validateBelongsToCatalog(product, command.catalog().getId());
		validateProductOwner(product, command.user().getId());

		product.update(command.name(), command.description());

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
		// todo: 판매 상품 상세 조회 메서드 추가되면 교체
		Product product = productRepository.findById(command.productId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		if (Objects.equals(product.getTxMethod(), ProductTxMethod.AUCTION)) {
			throw new ProductException(ProductErrorCode.ALREADY_AUCTION_TYPE);
		}

		product.changeToAuctionType(command.price());

		return product;
	}

	/**
	 * 파매 상품 삭제
	 * @param command 판매 상품 삭제 정보
	 * @author 윤정환
	 */
	@Transactional
	public void deleteProduct(ProductDeleteCommand command) {
		if (command.auction() != null && command.auction().isClosed()) {
			throw new ProductException(ProductErrorCode.AUCTION_NOT_CLOSED);
		}

		// todo: 판매 상품 상세 조회 메서드 추가되면 교체
		Product product = productRepository.findById(command.productId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		validateBelongsToCatalog(product, command.catalog().getId());
		validateProductOwner(product, command.user().getId());

		product.delete();

		// todo: 삭제된 정보 ES에 갱신
	}

	/**
	 * 판매 상품이 해당 카탈로그에 속해있는지 검증
	 * @param product 판매 상품 정보
	 * @param catalogId 카탈로그 id
	 * @author 윤정환
	 */
	private void validateBelongsToCatalog(Product product, Long catalogId) {
		if (!Objects.equals(product.getCatalog().getId(), catalogId)) {
			throw new ProductException(ProductErrorCode.NOT_BELONGS_TO_CATALOG);
		}
	}

	/**
	 * 판매 상품의 주인이 맞는지 검증
	 * @param product 판매 상품 정보
	 * @param userId 유저 id
	 * @author 윤정환
	 */
	private void validateProductOwner(Product product, Long userId) {
		if (!Objects.equals(product.getSeller().getId(), userId)) {
			throw new ProductException(ProductErrorCode.NOT_PRODUCT_OWNER);
		}
	}
}
