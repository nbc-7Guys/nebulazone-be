package nbc.chillguys.nebulazone.application.bid.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import nbc.chillguys.nebulazone.application.bid.dto.request.CreateBidRequest;
import nbc.chillguys.nebulazone.application.bid.dto.response.CreateBidResponse;
import nbc.chillguys.nebulazone.domain.auction.entity.Auction;
import nbc.chillguys.nebulazone.domain.auction.repository.AuctionRepository;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.bid.entity.Bid;
import nbc.chillguys.nebulazone.domain.bid.repository.BidRepository;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.catalog.repository.CatalogRepository;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.repository.ProductRepository;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.repository.UserRepository;

/**
 * 입찰 동시성 통합 테스트
 * - MySQL 비관적 락 기반 동시성 제어 검증
 * - H2 메모리 DB 환경에서 실제 트랜잭션/락 동작 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("입찰 동시성 통합 테스트")
class BidServiceIntegrationTest {

	@Autowired
	private BidService bidService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CatalogRepository catalogRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private BidRepository bidRepository;

	private static final Long INITIAL_POINT = 10_000_000L; // 1천만원
	private static final Long START_PRICE = 100_000L;      // 10만원
	private static final int THREAD_COUNT = 10;

	private User seller;
	private Auction auction;
	private List<User> bidders;

	@BeforeEach
	void setUp() {
		// 판매자 생성
		seller = createUser("seller@test.com", "판매자", INITIAL_POINT);

		// 카탈로그 및 상품 생성
		Catalog catalog = createCatalog("테스트 카탈로그");
		Product product = createProduct("테스트 상품", seller, catalog);

		// 경매 생성
		auction = createAuction(product);

		// 입찰자들 생성 (10명)
		bidders = createBidders(THREAD_COUNT);
	}

	@AfterEach
	void tearDown() {
		bidRepository.deleteAll();
		auctionRepository.deleteAll();
		productRepository.deleteAll();
		catalogRepository.deleteAll();
		userRepository.deleteAll();
	}

	/**
	 * 시나리오 1: 동일 경매에 여러 사용자가 동일한 가격으로 동시 입찰 - 비관적 락 검증
	 * 모든 사용자가 같은 높은 가격으로 입찰하여 비관적 락으로 인해 1명만 성공해야 함
	 */
	@Test
	@DisplayName("성공_동시입찰_동일가격_비관적락_1명만성공")
	void success_concurrentBidding_samePrice_pessimisticLock_onlyOneSuccess() throws InterruptedException {
		// given
		try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);

			// when - 모든 사용자가 동일한 높은 가격으로 동시 입찰
			final Long bidPrice = START_PRICE + 1_000_000L; // 모든 사용자가 110만원으로 입찰

			for (int i = 0; i < THREAD_COUNT; i++) {
				final User bidder = bidders.get(i);

				executor.submit(() -> {
					try {
						startLatch.await(); // 모든 스레드가 동시에 시작되도록 대기

						AuthUser authUser = createAuthUser(bidder);
						CreateBidRequest request = new CreateBidRequest(bidPrice);

						CreateBidResponse response = bidService.upsertBid(auction.getId(), authUser, request);

						if (response != null) {
							successCount.incrementAndGet();
							System.out.println("입찰 성공 - 사용자: " + bidder.getNickname() + ", 가격: " + bidPrice);
						}

					} catch (Exception e) {
						System.out.println(
							"입찰 실패 - 사용자: " + bidder.getNickname() + ", 가격: " + bidPrice + ", 원인: " + e.getClass()
								.getSimpleName() + " : " + e.getMessage());
						failCount.incrementAndGet();
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown(); // 모든 스레드 동시 시작
			endLatch.await(); // 모든 스레드 완료 대기

			// then
			Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
			List<Bid> allBids = bidRepository.findBidsByAuctionIdAndStatusBid(auction.getId());

			// 비관적 락으로 인해 동일한 가격에서는 1명만 성공해야 함
			assertThat(successCount.get()).isEqualTo(1);
			assertThat(failCount.get()).isEqualTo(THREAD_COUNT - 1);

			// 경매 현재가가 입찰가로 업데이트되었는지 검증
			assertThat(updatedAuction.getCurrentPrice()).isEqualTo(bidPrice);

			// 하나의 입찰만 생성되었는지 검증
			assertThat(allBids).hasSize(1);
			assertThat(allBids.getFirst().getPrice()).isEqualTo(bidPrice);

			System.out.println("=== 동일 가격 동시 입찰 테스트 결과 ===");
			System.out.println("성공한 입찰 수: " + successCount.get());
			System.out.println("실패한 입찰 수: " + failCount.get());
			System.out.println("최종 경매 현재가: " + updatedAuction.getCurrentPrice());
			System.out.println("생성된 입찰 수: " + allBids.size());
			System.out.println("입찰가: " + bidPrice + "원");
		}
	}

	/**
	 * 시나리오 1-2: 순차 입찰로 모든 사용자가 성공하는 케이스 검증
	 * 동시가 아닌 순차적으로 입찰하여 모든 입찰이 성공해야 함
	 */
	@Test
	@DisplayName("성공_순차입찰_모든사용자성공")
	void success_sequentialBidding_allUsersSuccess() {
		// given
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// when - 각 사용자가 순차적으로 더 높은 가격으로 입찰
		for (int i = 0; i < THREAD_COUNT; i++) {
			final User bidder = bidders.get(i);

			// 각 사용자마다 더 높은 가격: 20만, 30만, 40만... 110만원
			final Long bidPrice = START_PRICE + (i + 1) * 100_000L;

			try {
				AuthUser authUser = createAuthUser(bidder);
				CreateBidRequest request = new CreateBidRequest(bidPrice);

				CreateBidResponse response = bidService.upsertBid(auction.getId(), authUser, request);

				if (response != null) {
					successCount.incrementAndGet();
					System.out.println("순차 입찰 성공 - 사용자: " + bidder.getNickname() + ", 가격: " + bidPrice);
				}

			} catch (Exception e) {
				System.out.println(
					"순차 입찰 실패 - 사용자: " + bidder.getNickname() + ", 가격: " + bidPrice + ", 원인: " + e.getClass()
						.getSimpleName() + " : " + e.getMessage());
				failCount.incrementAndGet();
			}
		}

		// then
		Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
		List<Bid> allBids = bidRepository.findBidsByAuctionIdAndStatusBid(auction.getId());

		// 순차 입찰에서는 모든 입찰이 성공해야 함
		assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
		assertThat(failCount.get()).isEqualTo(0);

		// 경매 현재가가 가장 높은 입찰가로 업데이트되었는지 검증 (110만원)
		Long expectedHighestPrice = START_PRICE + THREAD_COUNT * 100_000L;
		assertThat(updatedAuction.getCurrentPrice()).isEqualTo(expectedHighestPrice);

		// 모든 사용자의 입찰이 생성되었는지 검증
		assertThat(allBids).hasSize(THREAD_COUNT);

		// 가장 높은 입찰가가 최종 현재가와 일치하는지 검증
		Long actualHighestPrice = allBids.stream()
			.mapToLong(Bid::getPrice)
			.max()
			.orElse(0L);
		assertThat(actualHighestPrice).isEqualTo(expectedHighestPrice);

		System.out.println("=== 순차 입찰 테스트 결과 ===");
		System.out.println("성공한 입찰 수: " + successCount.get());
		System.out.println("실패한 입찰 수: " + failCount.get());
		System.out.println("최종 경매 현재가: " + updatedAuction.getCurrentPrice());
		System.out.println("생성된 입찰 수: " + allBids.size());
		System.out.println("입찰가 범위: " + (START_PRICE + 100_000L) + "원 ~ " + expectedHighestPrice + "원");
	}

	/**
	 * 시나리오 2: 동일 사용자가 동일 경매에 연속으로 입찰 수정
	 * - 입찰 수정 로직의 동시성 안전성 검증
	 * - 포인트 차감/복구가 정확히 이루어지는지 확인
	 */
	@Test
	@DisplayName("성공_동일사용자연속입찰_포인트동시성검증")
	void success_sameUserConcurrentBidModification_pointsConcurrencyVerification() throws InterruptedException {
		// given
		User bidder = bidders.getFirst();
		AuthUser authUser = createAuthUser(bidder);

		try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(5);
			AtomicInteger successCount = new AtomicInteger(0);

			// when - 동일 사용자가 5번 연속으로 입찰가 증가
			for (int i = 0; i < 5; i++) {
				final Long bidPrice = START_PRICE + (i + 1) * 50_000L; // 15만, 20만, 25만, 30만, 35만원

				executor.submit(() -> {
					try {
						startLatch.await();

						CreateBidRequest request = new CreateBidRequest(bidPrice);
						CreateBidResponse response = bidService.upsertBid(auction.getId(), authUser, request);

						if (response != null) {
							successCount.incrementAndGet();
						}

					} catch (Exception e) {
						System.out.println("입찰 수정 실패 - 원인: " + e.getClass().getSimpleName() + " : " + e.getMessage());
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown();
			endLatch.await();

			// then
			User updatedBidder = userRepository.findById(bidder.getId()).orElseThrow();
			List<Bid> userBids = bidRepository.findBidsByAuctionIdAndStatusBid(auction.getId())
				.stream()
				.filter(bid -> bid.getUser().getId().equals(bidder.getId()))
				.toList();

			// 동일 사용자는 하나의 입찰만 가져야 함
			assertThat(userBids).hasSize(1);

			// 최종 입찰가 검증 (35만원)
			Bid finalBid = userBids.getFirst();
			Long expectedFinalPrice = START_PRICE + 5 * 50_000L;
			assertThat(finalBid.getPrice()).isEqualTo(expectedFinalPrice);

			// 포인트가 정확히 차감되었는지 검증
			Long expectedRemainingPoint = INITIAL_POINT - expectedFinalPrice;
			assertThat(updatedBidder.getPoint()).isEqualTo(expectedRemainingPoint);

			System.out.println("=== 연속 입찰 수정 테스트 결과 ===");
			System.out.println("성공한 수정 횟수: " + successCount.get());
			System.out.println("최종 입찰가: " + finalBid.getPrice());
			System.out.println("남은 포인트: " + updatedBidder.getPoint());
		}
	}

	/**
	 * 시나리오 3: 포인트 부족 상황에서의 동시 입찰
	 * - 포인트 부족 시 입찰 실패 처리가 정확한지 검증
	 * - 경계값에서의 동시성 이슈 확인
	 */
	@Test
	@DisplayName("성공_포인트부족동시입찰_경계값동시성검증")
	void success_insufficientPointsConcurrentBidding_boundaryValueConcurrencyVerification() throws
		InterruptedException {
		// given - 포인트가 적은 사용자 생성 (20만원만 보유)
		User lowPointBidder = createUser("lowpoint@test.com", "가난한입찰자", 200_000L);
		AuthUser authUser = createAuthUser(lowPointBidder);

		try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(3);
			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);

			// when - 포인트를 초과하는 금액으로 동시 입찰 시도
			for (int i = 0; i < 3; i++) {
				final Long bidPrice = 150_000L + i * 30_000L; // 15만, 18만, 21만원 (21만원은 포인트 부족)

				executor.submit(() -> {
					try {
						startLatch.await();

						CreateBidRequest request = new CreateBidRequest(bidPrice);
						CreateBidResponse response = bidService.upsertBid(auction.getId(), authUser, request);

						if (response != null) {
							successCount.incrementAndGet();
						}

					} catch (Exception e) {
						System.out.println(
							"포인트 부족으로 입찰 실패 - 원인: " + e.getClass().getSimpleName() + " : " + e.getMessage());
						failCount.incrementAndGet();
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown();
			endLatch.await();

			// then
			User updatedBidder = userRepository.findById(lowPointBidder.getId()).orElseThrow();
			List<Bid> userBids = bidRepository.findBidsByAuctionIdAndStatusBid(auction.getId())
				.stream()
				.filter(bid -> bid.getUser().getId().equals(lowPointBidder.getId()))
				.toList();

			// 포인트 범위 내에서만 입찰이 성공해야 함
			assertThat(successCount.get()).isLessThanOrEqualTo(2); // 최대 2번만 성공 가능
			assertThat(failCount.get()).isGreaterThan(0); // 최소 1번은 실패해야 함

			// 최종적으로 하나의 입찰만 존재해야 함 (수정 방식이므로)
			assertThat(userBids).hasSize(1);

			// 남은 포인트가 음수가 되지 않았는지 검증
			assertThat(updatedBidder.getPoint()).isGreaterThanOrEqualTo(0L);

			System.out.println("=== 포인트 부족 동시 입찰 테스트 결과 ===");
			System.out.println("성공한 입찰 수: " + successCount.get());
			System.out.println("실패한 입찰 수: " + failCount.get());
			System.out.println("남은 포인트: " + updatedBidder.getPoint());
			if (!userBids.isEmpty()) {
				System.out.println("최종 입찰가: " + userBids.getFirst().getPrice());
			}
		}
	}

	/**
	 * 시나리오 4: 현재 최고가보다 낮은 가격으로 동시 입찰
	 * - 입찰 검증 로직의 동시성 안전성 확인
	 * - 경쟁 상황에서의 가격 검증 정확성 테스트
	 */
	@Test
	@DisplayName("실패_낮은가격동시입찰_가격검증동시성확인")
	void fail_lowerPriceConcurrentBidding_priceValidationConcurrencyCheck() throws InterruptedException {
		// given - 먼저 높은 가격으로 입찰 설정 (50만원)
		User firstBidder = bidders.getFirst();
		AuthUser firstAuthUser = createAuthUser(firstBidder);
		CreateBidRequest firstRequest = new CreateBidRequest(500_000L);
		bidService.upsertBid(auction.getId(), firstAuthUser, firstRequest);

		try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(5);
			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);

			// when - 현재가(50만원)보다 낮은 가격으로 동시 입찰 시도
			for (int i = 1; i <= 5; i++) {
				final User bidder = bidders.get(i);
				final Long bidPrice = 300_000L + i * 10_000L; // 31만, 32만, 33만, 34만, 35만원 (모두 50만원보다 낮음)

				executor.submit(() -> {
					try {
						startLatch.await();

						AuthUser authUser = createAuthUser(bidder);
						CreateBidRequest request = new CreateBidRequest(bidPrice);

						CreateBidResponse response = bidService.upsertBid(auction.getId(), authUser, request);

						if (response != null) {
							successCount.incrementAndGet();
						}

					} catch (Exception e) {
						System.out.println(
							"낮은 가격 입찰 실패 - 원인: " + e.getClass().getSimpleName() + " : " + e.getMessage());
						failCount.incrementAndGet();
					} finally {
						endLatch.countDown();
					}
				});
			}

			startLatch.countDown();
			endLatch.await();

			// then
			Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
			List<Bid> allBids = bidRepository.findBidsByAuctionIdAndStatusBid(auction.getId());

			// 모든 낮은 가격 입찰이 실패해야 함
			assertThat(successCount.get()).isEqualTo(0);
			assertThat(failCount.get()).isEqualTo(5);

			// 경매 현재가가 변경되지 않았는지 검증 (50만원 유지)
			assertThat(updatedAuction.getCurrentPrice()).isEqualTo(500_000L);

			// 첫 번째 입찰자의 입찰만 존재해야 함
			assertThat(allBids).hasSize(1);
			assertThat(allBids.getFirst().getUser().getId()).isEqualTo(firstBidder.getId());

			System.out.println("=== 낮은 가격 동시 입찰 테스트 결과 ===");
			System.out.println("성공한 입찰 수: " + successCount.get());
			System.out.println("실패한 입찰 수: " + failCount.get());
			System.out.println("경매 현재가 유지: " + updatedAuction.getCurrentPrice());
			System.out.println("전체 입찰 수: " + allBids.size());
		}
	}

	// === 테스트 헬퍼 메서드들 ===

	private User createUser(String email, String nickname, Long point) {
		User user = User.builder()
			.email(email)
			.nickname(nickname)
			.point(point)
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.build();

		System.out.println("생성된 사용자 " + nickname + " 포인트: " + user.getPoint());
		return userRepository.save(user);
	}

	private Catalog createCatalog(String name) {
		Catalog catalog = Catalog.builder()
			.name(name)
			.description("테스트 카탈로그 설명")
			.type(CatalogType.CPU)
			.build();

		// productCode가 필수이므로 리플렉션으로 설정
		try {
			java.lang.reflect.Field productCodeField = Catalog.class.getDeclaredField("productCode");
			productCodeField.setAccessible(true);
			productCodeField.set(catalog, 12345L);
		} catch (Exception e) {
			throw new RuntimeException("Catalog productCode 설정 실패", e);
		}

		return catalogRepository.save(catalog);
	}

	private Product createProduct(String name, User seller, Catalog catalog) {
		Product product = Product.builder()
			.name(name)
			.description("테스트 상품 설명")
			.price(START_PRICE)
			.txMethod(ProductTxMethod.AUCTION)
			.seller(seller)
			.catalog(catalog)
			.build();
		return productRepository.save(product);
	}

	private Auction createAuction(Product product) {
		Auction auction = Auction.builder()
			.product(product)
			.startPrice(START_PRICE)
			.currentPrice(START_PRICE)
			.endTime(LocalDateTime.now().plusDays(7)) // 일주일 후 종료
			.isWon(false)
			.isDeleted(false)
			.build();
		return auctionRepository.save(auction);
	}

	private List<User> createBidders(int count) {
		return java.util.stream.IntStream.range(0, count)
			.mapToObj(i -> createUser(
				"bidder" + i + "@test.com",
				"입찰자" + i,
				INITIAL_POINT
			))
			.toList();
	}

	private AuthUser createAuthUser(User user) {
		return AuthUser.builder()
			.id(user.getId())
			.email(user.getEmail())
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
	}
}
