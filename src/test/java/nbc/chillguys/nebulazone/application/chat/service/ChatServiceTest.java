package nbc.chillguys.nebulazone.application.chat.service;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponses;
import nbc.chillguys.nebulazone.application.notification.dto.NotificationMessage;
import nbc.chillguys.nebulazone.application.notification.service.NotificationService;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@DisplayName("채팅 애플리케이션 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	// 테스트 상수
	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String BUYER_EMAIL = "buyer@test.com";
	private static final String PRODUCT_NAME = "테스트 상품";
	private static final Long PRODUCT_ID = 1L;
	private static final Long CHAT_ROOM_ID = 1L;
	private static final Long SELLER_ID = 1L;
	private static final Long BUYER_ID = 2L;
	@Mock
	private ChatDomainService chatDomainService;
	@Mock
	private ProductDomainService productDomainService;
	@Mock
	private UserDomainService userDomainService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	@InjectMocks
	private ChatService chatService;
	// 공통 테스트 픽스처
	private User seller;
	private User buyer;
	private Product product;
	private ChatRoom chatRoom;

	@BeforeEach
	void setUp() {
		seller = User.builder()
			.email(SELLER_EMAIL)
			.nickname("판매자")
			.oAuthType(OAuthType.KAKAO)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(List.of(Address.builder()
				.addressNickname("테스트주소")
				.roadAddress("테스트도로명")
				.detailAddress("테스트상세주소")
				.build()))
			.point(0)
			.build();
		ReflectionTestUtils.setField(seller, "id", SELLER_ID);

		buyer = User.builder()
			.email(BUYER_EMAIL)
			.nickname("구매자")
			.oAuthType(OAuthType.KAKAO)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(List.of(Address.builder()
				.addressNickname("테스트주소")
				.roadAddress("테스트도로명")
				.detailAddress("테스트상세주소")
				.build()))
			.point(0)
			.build();
		ReflectionTestUtils.setField(buyer, "id", BUYER_ID);

		Catalog catalog = Catalog.builder()
			.name("테스트 카탈로그")
			.description("테스트 카탈로그 설명")
			.type(CatalogType.CPU)
			.build();
		ReflectionTestUtils.setField(catalog, "id", 1L);

		product = Product.builder()
			.name(PRODUCT_NAME)
			.txMethod(ProductTxMethod.DIRECT)
			.seller(seller)
			.catalog(catalog)
			.build();
		ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

		chatRoom = ChatRoom.builder()
			.product(product)
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);
	}

	@Nested
	@DisplayName("채팅방 생성 또는 기존 채팅방 조회")
	class GetOrCreateTest {

		@Test
		@DisplayName("새로운 채팅방 생성 성공")
		void success_getOrCreate_newChatRoom() {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);

			given(productDomainService.findAvailableProductById(PRODUCT_ID)).willReturn(product);
			given(chatDomainService.findExistingChatRoom(BUYER_ID, PRODUCT_ID)).willReturn(Optional.empty());
			given(userDomainService.findActiveUserByEmail(BUYER_EMAIL)).willReturn(buyer);
			given(chatDomainService.createChatRoom(product, buyer, seller)).willReturn(chatRoom);
			willDoNothing().given(notificationService).sendNotificationToUser(any(), any());

			// when
			CreateChatRoomResponse result = chatService.getOrCreate(buyer, request);

			// then
			assertThat(result.chatRoomId()).isEqualTo(CHAT_ROOM_ID);
			verify(productDomainService, times(2)).findAvailableProductById(PRODUCT_ID);
			verify(chatDomainService).findExistingChatRoom(BUYER_ID, PRODUCT_ID);
			verify(userDomainService).findActiveUserByEmail(BUYER_EMAIL);
			verify(chatDomainService).createChatRoom(product, buyer, seller);
			verify(notificationService).sendNotificationToUser(eq(SELLER_ID), any(NotificationMessage.class));
		}

		@Test
		@DisplayName("기존 채팅방 조회 성공")
		void success_getOrCreate_existingChatRoom() {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);

			given(productDomainService.findAvailableProductById(PRODUCT_ID)).willReturn(product);
			given(chatDomainService.findExistingChatRoom(BUYER_ID, PRODUCT_ID)).willReturn(Optional.of(chatRoom));

			// when
			CreateChatRoomResponse result = chatService.getOrCreate(buyer, request);

			// then
			assertThat(result.chatRoomId()).isEqualTo(CHAT_ROOM_ID);
			verify(productDomainService).findAvailableProductById(PRODUCT_ID);
			verify(chatDomainService).findExistingChatRoom(BUYER_ID, PRODUCT_ID);
			verify(userDomainService, never()).findActiveUserByEmail(any());
			verify(chatDomainService, never()).createChatRoom(any(), any(), any());
			verify(notificationService, never()).sendNotificationToUser(any(), any());
		}

		@Test
		@DisplayName("자기 자신과 채팅 시도 시 예외 발생")
		void fail_getOrCreate_selfChat() {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);
			given(productDomainService.findAvailableProductById(PRODUCT_ID)).willReturn(product);

			// when & then
			assertThatThrownBy(() -> chatService.getOrCreate(seller, request))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CANNOT_CHAT_WITH_SELF);

			verify(productDomainService).findAvailableProductById(PRODUCT_ID);
			verify(chatDomainService, never()).findExistingChatRoom(any(), any());
		}
	}

	@Nested
	@DisplayName("새로운 채팅방 생성")
	class CreateChatRoomTest {

		@Test
		@DisplayName("새로운 채팅방 생성 성공")
		void success_createChatRoom() {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);

			given(productDomainService.findAvailableProductById(PRODUCT_ID)).willReturn(product);
			given(userDomainService.findActiveUserByEmail(BUYER_EMAIL)).willReturn(buyer);
			given(chatDomainService.createChatRoom(product, buyer, seller)).willReturn(chatRoom);
			willDoNothing().given(notificationService).sendNotificationToUser(any(), any());

			// when
			ChatRoom result = chatService.createChatRoom(buyer, request);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getId()).isEqualTo(CHAT_ROOM_ID);
			assertThat(result.getProduct().getId()).isEqualTo(PRODUCT_ID);
			verify(productDomainService).findAvailableProductById(PRODUCT_ID);
			verify(userDomainService).findActiveUserByEmail(BUYER_EMAIL);
			verify(chatDomainService).createChatRoom(product, buyer, seller);
			verify(notificationService).sendNotificationToUser(eq(SELLER_ID), any(NotificationMessage.class));
		}
	}

	@Nested
	@DisplayName("참여중인 채팅방 목록 조회")
	class FindChatRoomsTest {

		@Test
		@DisplayName("참여중인 채팅방 목록 조회 성공")
		void success_findChatRooms() {
			// given
			ChatRoomInfo chatRoomInfo1 = new ChatRoomInfo(
				PRODUCT_NAME, "판매자", CHAT_ROOM_ID, 1L, PRODUCT_ID, 100000L, false, "test-image-url-1");
			ChatRoomInfo chatRoomInfo2 = new ChatRoomInfo(
				"다른 상품", "판매자", 2L, 1L, 2L, 200000L, false, "test-image-url-2");
			List<ChatRoomInfo> chatRoomInfos = List.of(chatRoomInfo1, chatRoomInfo2);
			given(chatDomainService.findChatRooms(buyer)).willReturn(chatRoomInfos);

			// when
			FindChatRoomResponses result = chatService.findChatRooms(buyer);

			// then
			assertThat(result.chatRooms()).hasSize(2);
			assertThat(result.chatRooms().getFirst().chatRoomId()).isEqualTo(CHAT_ROOM_ID);
			verify(chatDomainService).findChatRooms(buyer);
		}

		@Test
		@DisplayName("참여중인 채팅방이 없음")
		void empty_findChatRooms() {
			// given
			given(chatDomainService.findChatRooms(buyer)).willReturn(List.of());

			// when
			FindChatRoomResponses result = chatService.findChatRooms(buyer);

			// then
			assertThat(result.chatRooms()).isEmpty();
			verify(chatDomainService).findChatRooms(buyer);
		}
	}

	@Nested
	@DisplayName("채팅 기록 조회")
	class FindChatHistoriesTest {

		@Test
		@DisplayName("채팅 기록 조회 성공")
		void success_findChatHistories() {
			// given
			int size = 30;
			FindChatHistoryResponse response1 = new FindChatHistoryResponse(
				1L, "안녕하세요", LocalDateTime.now(), MessageType.TEXT);
			FindChatHistoryResponse response2 = new FindChatHistoryResponse(
				2L, "반갑습니다", LocalDateTime.now(), MessageType.TEXT);
			List<FindChatHistoryResponse> responses = List.of(response1, response2);

			willDoNothing().given(chatDomainService).validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID);
			given(chatDomainService.findChatHistoryResponses(CHAT_ROOM_ID, null, size)).willReturn(responses);

			// when
			List<FindChatHistoryResponse> result = chatService.findChatHistories(buyer, CHAT_ROOM_ID, null, size);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).message()).isEqualTo("안녕하세요");
			assertThat(result.get(1).message()).isEqualTo("반갑습니다");
			verify(chatDomainService).validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID);
			verify(chatDomainService).findChatHistoryResponses(CHAT_ROOM_ID, null, size);
		}

		@Test
		@DisplayName("채팅 기록 조회 실패 - 접근 권한 없음")
		void fail_findChatHistories_accessDenied() {
			// given
			Long lastId = null;
			int size = 30;

			willThrow(new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED))
				.given(chatDomainService).validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID);

			// when & then
			assertThatThrownBy(() ->
				chatService.findChatHistories(buyer, CHAT_ROOM_ID, lastId, size))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatDomainService).validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID);
			verify(chatDomainService, never()).findChatHistoryResponses(any(), any(), anyInt());
		}
	}

	@Nested
	@DisplayName("채팅방 나가기")
	class ExitChatRoomTest {

		@Test
		@DisplayName("채팅방 나가기 성공")
		void success_exitChatRoom() {
			// given
			String userEmail = BUYER_EMAIL;
			given(chatDomainService.deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID))
				.willReturn(userEmail);
			willDoNothing().given(messagingTemplate).convertAndSend(anyString(), anyString());

			// when
			chatService.exitChatRoom(buyer, CHAT_ROOM_ID);

			// then
			verify(chatDomainService).deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID);
			verify(messagingTemplate).convertAndSend(
				eq("/topic/chat/" + CHAT_ROOM_ID),
				contains(userEmail + " 님이 채팅방을 나갔습니다.")
			);
		}

		@Test
		@DisplayName("채팅방 나가기 실패 - 접근 권한 없음")
		void fail_exitChatRoom_accessDenied() {
			// given
			given(chatDomainService.deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID))
				.willThrow(new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

			// when & then
			assertThatThrownBy(() ->
				chatService.exitChatRoom(buyer, CHAT_ROOM_ID))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatDomainService).deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID);
			verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
		}
	}
}
