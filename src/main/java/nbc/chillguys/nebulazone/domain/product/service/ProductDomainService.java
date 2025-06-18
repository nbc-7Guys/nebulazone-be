package nbc.chillguys.nebulazone.domain.product.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.domain.product.dto.ChangeToAuctionTypeCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductCreateCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductDeleteCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductFindQuery;
import nbc.chillguys.nebulazone.domain.product.dto.ProductPurchaseCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductSearchCommand;
import nbc.chillguys.nebulazone.domain.product.dto.ProductUpdateCommand;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.product.exception.ProductException;
import nbc.chillguys.nebulazone.domain.product.repository.ProductEsRepository;
import nbc.chillguys.nebulazone.domain.product.repository.ProductRepository;
import nbc.chillguys.nebulazone.domain.product.vo.ProductDocument;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDomainService {

	private final ProductRepository productRepository;
	private final ProductEsRepository productEsRepository;

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
		return productRepository.findActiveProductById(productId)
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

		product.validNotSold();
		product.validPurchasable();

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

		product.validBelongsToCatalog(command.catalog().getId());
		product.validNotProductOwner(command.user().getId());

		product.update(command.name(), command.description(), command.imageUrls());

		return product;
	}

	/**
	 * 판매 방식 옥션으로 변경
	 * @param command 판매 싱픔 정보
	 * @return product
	 * @author 윤정환
	 */
	@Transactional
	public Product changeToAuctionType(ChangeToAuctionTypeCommand command) {
		Product product = findActiveProductById(command.productId());

		product.validBelongsToCatalog(command.catalog().getId());
		product.validNotProductOwner(command.user().getId());

		product.changeToAuctionType(command.price());

		return product;
	}

	/**
	 * 판매 상품 삭제
	 * @param command 판매 상품 삭제 정보
	 * @author 윤정환
	 */
	@Transactional
	public Product deleteProduct(ProductDeleteCommand command) {
		Product product = findActiveProductById(command.productId());

		product.validBelongsToCatalog(command.catalog().getId());
		product.validNotProductOwner(command.user().getId());

		product.delete();

		return product;
	}

	@Transactional
	public void purchaseProduct(ProductPurchaseCommand command) {
		Product product = findAvailableProductById(command.productId());

		product.purchase();

		product.getSeller().addPoint(product.getPrice());
	}

	/**
	 * Elasticsearch에 상품 저장
	 * @param product 상품
	 * @author 이승현
	 */
	@Transactional
	public void saveProductToEs(Product product) {
		productEsRepository.save(ProductDocument.from(product));
	}

	/**
	 * 상품 검색
	 * @param command keyword, txMethod, priceFrom, priceTo, page, size
	 * @return 상품 목록
	 * @author 이승현
	 */
	public Page<ProductDocument> searchProduct(ProductSearchCommand command) {
		Pageable pageable = PageRequest.of(command.page() - 1, command.size());

		return productEsRepository.searchProduct(command.productName(), command.sellerNickname(), command.txMethod(),
			command.priceFrom(), command.priceTo(), pageable);
	}

	/**
	 * Elasticsearch에 상품 삭제
	 * @param productId 상품 id
	 * @author 이승현
	 */
	@Transactional
	public void deleteProductFromEs(Long productId) {
		productEsRepository.deleteById(productId);
	}

	/**
	 * 상품 조회</br>
	 * 유저와 이미지도 함께 조회
	 * @param query 조회 정보
	 * @return product
	 * @author 이승현
	 */
	public Product getProductByIdWithUserAndImages(ProductFindQuery query) {
		Product product = productRepository.findActiveProductByIdWithUserAndImages(query.productId())
			.orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

		product.validBelongsToCatalog(query.catalogId());

		return product;
	}
}
