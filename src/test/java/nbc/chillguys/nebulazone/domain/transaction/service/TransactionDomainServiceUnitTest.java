package nbc.chillguys.nebulazone.domain.transaction.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.products.entity.Product;
import nbc.chillguys.nebulazone.domain.products.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionCreateCommand;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindAllInfo;
import nbc.chillguys.nebulazone.domain.transaction.dto.TransactionFindDetailInfo;
import nbc.chillguys.nebulazone.domain.transaction.entity.Transaction;
import nbc.chillguys.nebulazone.domain.transaction.entity.TransactionMethod;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionErrorCode;
import nbc.chillguys.nebulazone.domain.transaction.exception.TransactionException;
import nbc.chillguys.nebulazone.domain.transaction.repository.TransactionRepository;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("거래 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class TransactionDomainServiceUnitTest {

	@Mock
	TransactionRepository transactionRepository;

	@InjectMocks
	TransactionDomainService transactionDomainService;

	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String SELLER_NICKNAME = "판매자";
	private static final String BUYER_EMAIL = "buyer@test.com";
	private static final String BUYER_NICKNAME = "구매자";
	private static final String OTHER_USER_EMAIL = "other@test.com";
	private static final String OTHER_USER_NICKNAME = "다른사용자";
	private static final String PRODUCT_NAME = "테스트 CPU";
	private static final String PRODUCT_DESCRIPTION = "설명";
	private static final String CATALOG_NAME = "테스트 CPU";
	private static final String CATALOG_DESCRIPTION = "카탈로그";
	private static final Long TRANSACTION_PRICE = 500000L;
	private static final String DIRECT_METHOD = "DIRECT";
	private static final String AUCTION_METHOD = "AUCTION";
	private static final String INVALID_METHOD = "INVALID";

	private User seller;
	private User buyer;
	private User otherUser;
	private Catalog catalog;
	private Product product;

	@BeforeEach
	void setUp() {
		seller = createUser(1L, SELLER_EMAIL, SELLER_NICKNAME);
		buyer = createUser(2L, BUYER_EMAIL, BUYER_NICKNAME);
		otherUser = createUser(3L, OTHER_USER_EMAIL, OTHER_USER_NICKNAME);
		catalog = createCatalog(1L, CATALOG_NAME, CATALOG_DESCRIPTION);
		product = createProduct(1L, PRODUCT_NAME, PRODUCT_DESCRIPTION, TRANSACTION_PRICE, seller, catalog);
	}

	@Nested
	@DisplayName("거래내역 생성")
	class CreateTransactionTest {

		@Test
		@DisplayName("거래내역 생성 성공 - 직거래")
		void success_createTransaction_direct() {
			// given
			TransactionCreateCommand command = TransactionCreateCommand.of(
				buyer, product, DIRECT_METHOD, TRANSACTION_PRICE);
			Transaction expectedTransaction = createTransaction(1L, buyer, product,
				TransactionMethod.DIRECT, TRANSACTION_PRICE);

			given(transactionRepository.save(any(Transaction.class))).willReturn(expectedTransaction);

			// when
			Transaction result = transactionDomainService.createTransaction(command);

			// then
			assertThat(result.getUser()).isEqualTo(buyer);
			assertThat(result.getProduct()).isEqualTo(product);
			assertThat(result.getMethod()).isEqualTo(TransactionMethod.DIRECT);
			assertThat(result.getPrice()).isEqualTo(TRANSACTION_PRICE);
		}

		@Test
		@DisplayName("거래내역 생성 성공 - 경매")
		void success_createTransaction_auction() {
			// given
			TransactionCreateCommand command = TransactionCreateCommand.of(
				buyer, product, AUCTION_METHOD, TRANSACTION_PRICE);
			Transaction expectedTransaction = createTransaction(2L, buyer, product,
				TransactionMethod.AUCTION, TRANSACTION_PRICE);

			given(transactionRepository.save(any(Transaction.class))).willReturn(expectedTransaction);

			// when
			Transaction result = transactionDomainService.createTransaction(command);

			// then
			assertThat(result.getUser()).isEqualTo(buyer);
			assertThat(result.getProduct()).isEqualTo(product);
			assertThat(result.getMethod()).isEqualTo(TransactionMethod.AUCTION);
			assertThat(result.getPrice()).isEqualTo(TRANSACTION_PRICE);
		}

		@Test
		@DisplayName("거래내역 생성 실패 - 유효하지 않은 거래 방법")
		void fail_createTransaction_invalidMethod() {
			// given
			TransactionCreateCommand command = TransactionCreateCommand.of(
				buyer, product, INVALID_METHOD, TRANSACTION_PRICE);

			// when & then
			assertTransactionException(() -> transactionDomainService.createTransaction(command),
				TransactionErrorCode.INVALID_TX_METHOD);
		}
	}

	@Nested
	@DisplayName("내 거래내역 조회")
	class FindMyTransactionsTest {

		@Test
		@DisplayName("내 거래내역 전체 조회 성공")
		void success_findMyTransactions() {
			// given
			int page = 0;
			int size = 10;
			List<TransactionFindAllInfo> transactionInfoList = createTransactionFindAllInfoList();
			Page<TransactionFindAllInfo> pageResult = new PageImpl<>(transactionInfoList);

			given(transactionRepository.findTransactionsWithProductAndUser(buyer, page, size))
				.willReturn(pageResult);

			// when
			Page<TransactionFindAllInfo> result = transactionDomainService.findMyTransactions(buyer, page, size);

			// then
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent().get(0).txPrice()).isEqualTo(TRANSACTION_PRICE);
			assertThat(result.getContent().get(0).productName()).isEqualTo(PRODUCT_NAME);
			assertThat(result.getContent().get(0).txMethod()).isEqualTo(TransactionMethod.DIRECT);
			assertThat(result.getContent().get(1).txMethod()).isEqualTo(TransactionMethod.AUCTION);
		}

	}

	@Nested
	@DisplayName("내 거래내역 상세 조회")
	class FindMyTransactionTest {

		@Test
		@DisplayName("내 거래내역 상세 조회 성공")
		void success_findMyTransaction() {
			// given
			Long transactionId = 100L;
			TransactionFindDetailInfo detailInfo = createTransactionFindDetailInfo(transactionId);

			given(transactionRepository.findTransactionWithProductAndUser(buyer, transactionId))
				.willReturn(Optional.of(detailInfo));

			// when
			TransactionFindDetailInfo result = transactionDomainService.findMyTransaction(buyer, transactionId);

			// then
			assertThat(result.txId()).isEqualTo(transactionId);
			assertThat(result.txPrice()).isEqualTo(TRANSACTION_PRICE);
			assertThat(result.productName()).isEqualTo(PRODUCT_NAME);
			assertThat(result.sellerNickname()).isEqualTo(SELLER_NICKNAME);
			assertThat(result.txMethod()).isEqualTo(TransactionMethod.DIRECT);
		}

		@Test
		@DisplayName("내 거래내역 상세 조회 실패 - 존재하지 않는 거래")
		void fail_findMyTransaction_notFound() {
			// given
			Long transactionId = 999L;
			given(transactionRepository.findTransactionWithProductAndUser(buyer, transactionId))
				.willReturn(Optional.empty());

			// when & then
			assertTransactionException(() -> transactionDomainService.findMyTransaction(buyer, transactionId),
				TransactionErrorCode.NOT_FOUNT_TRANSACTION);
		}

		@Test
		@DisplayName("내 거래내역 상세 조회 실패 - 다른 사용자의 거래")
		void fail_findMyTransaction_otherUserTransaction() {
			// given
			Long transactionId = 100L;
			given(transactionRepository.findTransactionWithProductAndUser(otherUser, transactionId))
				.willReturn(Optional.empty());

			// when & then
			assertTransactionException(() -> transactionDomainService.findMyTransaction(otherUser, transactionId),
				TransactionErrorCode.NOT_FOUNT_TRANSACTION);
		}
	}

	// 팩토리 메서드들
	private User createUser(Long id, String email, String nickname) {
		User user = User.builder()
			.email(email)
			.nickname(nickname)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	private Catalog createCatalog(Long id, String name, String description) {
		Catalog catalog = Catalog.builder()
			.name(name)
			.description(description)
			.type(CatalogType.CPU)
			.build();
		ReflectionTestUtils.setField(catalog, "id", id);
		return catalog;
	}

	private Product createProduct(Long id, String name, String description, Long price, User seller, Catalog catalog) {
		Product product = Product.builder()
			.name(name)
			.description(description)
			.price(price)
			.txMethod(ProductTxMethod.DIRECT)
			.seller(seller)
			.catalog(catalog)
			.build();
		ReflectionTestUtils.setField(product, "id", id);
		return product;
	}

	private Transaction createTransaction(Long id, User user, Product product,
		TransactionMethod method, Long price) {
		Transaction transaction = Transaction.builder()
			.user(user)
			.product(product)
			.method(method)
			.price(price)
			.build();
		ReflectionTestUtils.setField(transaction, "id", id);
		return transaction;
	}

	private List<TransactionFindAllInfo> createTransactionFindAllInfoList() {
		return List.of(
			new TransactionFindAllInfo(1L, TRANSACTION_PRICE, TransactionMethod.DIRECT,
				LocalDateTime.now(), PRODUCT_NAME, true),
			new TransactionFindAllInfo(2L, TRANSACTION_PRICE + 50000L, TransactionMethod.AUCTION,
				LocalDateTime.now().minusHours(1), PRODUCT_NAME, true)
		);
	}

	private TransactionFindDetailInfo createTransactionFindDetailInfo(Long transactionId) {
		return new TransactionFindDetailInfo(transactionId, 1L, SELLER_NICKNAME, SELLER_EMAIL,
			TRANSACTION_PRICE, LocalDateTime.now(), TransactionMethod.DIRECT,
			1L, PRODUCT_NAME, LocalDateTime.now().minusDays(1), true);
	}

	// 공통 예외 검증 헬퍼 메서드
	private void assertTransactionException(Runnable executable, TransactionErrorCode expectedErrorCode) {
		assertThatThrownBy(executable::run)
			.isInstanceOf(TransactionException.class)
			.extracting("errorCode")
			.isEqualTo(expectedErrorCode);
	}
}
