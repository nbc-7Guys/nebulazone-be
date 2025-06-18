package nbc.chillguys.nebulazone.application.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.service.ProductDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@DisplayName("채팅 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	private final LocalDateTime now = LocalDateTime.now();
	@Mock
	private ChatDomainService chatDomainService;
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	@Mock
	private ProductDomainService productDomainService;
	@Mock
	private UserDomainService userDomainService;
	@InjectMocks
	private ChatService chatService;
	private User buyer;
	private User seller;
	private Product product;
	private Catalog catalog;
	private ChatRoom chatRoom;
	private AuthUser authUser;

	@BeforeEach
	void setUp() {
		// 카탈로그 생성
		catalog = Catalog.builder()
			.name("테스트 카탈로그")
			.description("카탈로그 설명")
			.type(CatalogType.CPU)
			.build();
		ReflectionTestUtils.setField(catalog, "id", 1L);

		// 판매자 생성
		seller = User.builder()
			.email("seller@test.com")
			.nickname("판매자")
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
		ReflectionTestUtils.setField(seller, "id", 2L);

		// 구매자 생성
		buyer = User.builder()
			.email("buyer@test.com")
			.nickname("구매자")
			.oAuthType(OAuthType.DOMAIN)
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
		ReflectionTestUtils.setField(buyer, "id", 1L);

		// 상품 생성
		product = Product.builder()
			.name("테스트 상품")
			.description("상품 설명")
			.price(10000L)
			.txMethod(ProductTxMethod.DIRECT)
			.seller(seller)
			.catalog(catalog)
			.build();
		ReflectionTestUtils.setField(product, "id", 1L);

		// 채팅방 생성
		chatRoom = ChatRoom.builder()
			.product(product)
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", 1L);

		// 인증 사용자
		authUser = AuthUser.builder()
			.id(1L)
			.email("buyer@test.com")
			.roles(Set.of(UserRole.ROLE_USER))
			.build();
	}

	@Nested
	@DisplayName("채팅방 생성 또는 조회 테스트")
	class GetOrCreateTest {

		@Test
		@DisplayName("새로운 채팅방 생성 성공")
		void success_createNewChatRoom() {
			// Given
			CreateChatRoomRequest request = new CreateChatRoomRequest(1L);

			given(productDomainService.findAvailableProductById(1L)).willReturn(product);
			given(chatDomainService.findExistingChatRoom(1L, 1L)).willReturn(Optional.empty());
			given(userDomainService.findActiveUserByEmail("buyer@test.com")).willReturn(buyer);
			given(chatDomainService.createChatRoom(product, buyer, seller)).willReturn(chatRoom);

			// When
			CreateChatRoomResponse response = chatService.getOrCreate(authUser, request);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.chatRoomId()).isEqualTo(1L);
			assertThat(response.productId()).isEqualTo(1L);
			assertThat(response.productName()).isEqualTo("테스트 상품");
			assertThat(response.sellerId()).isEqualTo(2L);
			assertThat(response.sellerName()).isEqualTo("판매자");

			verify(chatDomainService).findExistingChatRoom(1L, 1L);
			verify(chatDomainService).createChatRoom(product, buyer, seller);
		}

		@Test
		@DisplayName("기존 채팅방 조회 성공")
		void success_findExistingChatRoom() {
			// Given
			CreateChatRoomRequest request = new CreateChatRoomRequest(1L);

			given(productDomainService.findAvailableProductById(1L)).willReturn(product);
			given(chatDomainService.findExistingChatRoom(1L, 1L)).willReturn(Optional.of(chatRoom));

			// When
			CreateChatRoomResponse response = chatService.getOrCreate(authUser, request);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.chatRoomId()).isEqualTo(1L);
			assertThat(response.productId()).isEqualTo(1L);
			assertThat(response.productName()).isEqualTo("테스트 상품");

			verify(chatDomainService).findExistingChatRoom(1L, 1L);
			verify(chatDomainService, never()).createChatRoom(any(), any(), any());
		}

		@Test
		@DisplayName("채팅방 생성 실패 - 자신의 상품과 채팅")
		void fail_createChatRoom_cannotChatWithSelf() {
			// Given
			CreateChatRoomRequest request = new CreateChatRoomRequest(1L);

			// 구매자와 판매자가 같은 경우
			AuthUser sellerAuthUser = AuthUser.builder()
				.id(2L)
				.email("seller@test.com")
				.roles(Set.of(UserRole.ROLE_USER))
				.build();

			given(productDomainService.findAvailableProductById(1L)).willReturn(product);

			// When & Then
			ChatException exception = assertThrows(ChatException.class,
				() -> chatService.getOrCreate(sellerAuthUser, request));

			assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.CANNOT_CHAT_WITH_SELF);

			verify(chatDomainService, never()).findExistingChatRoom(any(), any());
			verify(chatDomainService, never()).createChatRoom(any(), any(), any());
		}
	}

	@Nested
	@DisplayName("채팅방 목록 조회 테스트")
	class FindChatRoomsTest {

		@Test
		@DisplayName("채팅방 목록 조회 성공")
		void success_findChatRooms() {
			// Given
			List<ChatRoomInfo> chatRoomInfos = List.of(
				new ChatRoomInfo("테스트 상품", "판매자", 1L, 1L, 1L, 10000L, false),
				new ChatRoomInfo("테스트 상품2", "판매자2", 2L, 2L, 2L, 20000L, false)
			);

			given(chatDomainService.findChatRooms(authUser)).willReturn(chatRoomInfos);

			// When
			FindChatRoomResponses response = chatService.findChatRooms(authUser);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.chatRooms()).hasSize(2);
			assertThat(response.chatRooms().get(0).productName()).isEqualTo("테스트 상품");
			assertThat(response.chatRooms().get(0).sellerName()).isEqualTo("판매자");
			assertThat(response.chatRooms().get(0).chatRoomId()).isEqualTo(1L);
			assertThat(response.chatRooms().get(1).productName()).isEqualTo("테스트 상품2");
			assertThat(response.chatRooms().get(1).sellerName()).isEqualTo("판매자2");
			assertThat(response.chatRooms().get(1).chatRoomId()).isEqualTo(2L);

			verify(chatDomainService).findChatRooms(authUser);
		}

		@Test
		@DisplayName("채팅방 목록 조회 성공 - 빈 목록")
		void success_findChatRooms_emptyList() {
			// Given
			given(chatDomainService.findChatRooms(authUser)).willReturn(List.of());

			// When
			FindChatRoomResponses response = chatService.findChatRooms(authUser);

			// Then
			assertThat(response).isNotNull();
			assertThat(response.chatRooms()).isEmpty();

			verify(chatDomainService).findChatRooms(authUser);
		}
	}

	@Nested
	@DisplayName("채팅 기록 조회 테스트")
	class FindChatHistoriesTest {

		@Test
		@DisplayName("채팅 기록 조회 성공")
		void success_findChatHistories() {
			// Given
			Long roomId = 1L;
			List<FindChatHistoryResponse> responses = List.of(
				new FindChatHistoryResponse(1L, "안녕하세요", now),
				new FindChatHistoryResponse(2L, "반갑습니다", now.plusMinutes(1))
			);

			willDoNothing().given(chatDomainService).validateUserAccessToChatRoom(authUser, roomId);
			given(chatDomainService.findChatHistoryResponses(roomId)).willReturn(responses);

			// When
			List<FindChatHistoryResponse> result = chatService.findChatHistories(authUser, roomId);

			// Then
			assertThat(result).isNotNull();
			assertThat(result).hasSize(2);
			assertThat(result.get(0).senderId()).isEqualTo(1L);
			assertThat(result.get(0).message()).isEqualTo("안녕하세요");
			assertThat(result.get(1).senderId()).isEqualTo(2L);
			assertThat(result.get(1).message()).isEqualTo("반갑습니다");

			verify(chatDomainService).validateUserAccessToChatRoom(authUser, roomId);
			verify(chatDomainService).findChatHistoryResponses(roomId);
		}

		@Test
		@DisplayName("채팅 기록 조회 실패 - 접근 권한 없음")
		void fail_findChatHistories_accessDenied() {
			// Given
			Long roomId = 1L;

			willThrow(new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED))
				.given(chatDomainService).validateUserAccessToChatRoom(authUser, roomId);

			// When & Then
			ChatException exception = assertThrows(ChatException.class,
				() -> chatService.findChatHistories(authUser, roomId));

			assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatDomainService).validateUserAccessToChatRoom(authUser, roomId);
			verify(chatDomainService, never()).findChatHistoryResponses(any());
		}
	}

	@Nested
	@DisplayName("채팅방 나가기 테스트")
	class ExitChatRoomTest {

		@Test
		@DisplayName("채팅방 나가기 성공")
		void success_exitChatRoom() {
			// Given
			Long roomId = 1L;
			String leftUserEmail = "buyer@test.com";

			given(chatDomainService.deleteUserFromChatRoom(authUser.getId(), roomId))
				.willReturn(leftUserEmail);

			// When
			chatService.exitChatRoom(authUser, roomId);

			// Then
			verify(chatDomainService).deleteUserFromChatRoom(authUser.getId(), roomId);
			verify(messagingTemplate).convertAndSend(
				eq("/topic/chat/" + roomId),
				eq(leftUserEmail + " 님이 채팅방을 나갔습니다.")
			);
		}

		@Test
		@DisplayName("채팅방 나가기 실패 - 접근 권한 없음")
		void fail_exitChatRoom_accessDenied() {
			// Given
			Long roomId = 1L;

			given(chatDomainService.deleteUserFromChatRoom(authUser.getId(), roomId))
				.willThrow(new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED));

			// When & Then
			ChatException exception = assertThrows(ChatException.class,
				() -> chatService.exitChatRoom(authUser, roomId));

			assertThat(exception.getErrorCode()).isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatDomainService).deleteUserFromChatRoom(authUser.getId(), roomId);
			verify(messagingTemplate, never()).convertAndSend(anyString(), anyString());
		}
	}
}
