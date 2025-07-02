package nbc.chillguys.nebulazone.domain.chat.service;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatHistory;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoom;
import nbc.chillguys.nebulazone.domain.chat.entity.ChatRoomUser;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomHistoryRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomRepository;
import nbc.chillguys.nebulazone.domain.chat.repository.ChatRoomUserRepository;
import nbc.chillguys.nebulazone.domain.product.entity.Product;
import nbc.chillguys.nebulazone.domain.product.entity.ProductTxMethod;
import nbc.chillguys.nebulazone.domain.product.exception.ProductErrorCode;
import nbc.chillguys.nebulazone.domain.product.exception.ProductException;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;

@DisplayName("채팅 도메인 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatDomainServiceTest {

	private static final String SELLER_EMAIL = "seller@test.com";
	private static final String BUYER_EMAIL = "buyer@test.com";
	private static final String PRODUCT_NAME = "테스트 상품";
	private static final Long PRODUCT_ID = 1L;
	private static final Long CHAT_ROOM_ID = 1L;
	private static final Long SELLER_ID = 1L;
	private static final Long BUYER_ID = 2L;
	@Mock
	private ChatRoomRepository chatRoomRepository;
	@Mock
	private ChatRoomUserRepository chatRoomUserRepository;
	@Mock
	private ChatRoomHistoryRepository chatRoomHistoryRepository;
	@InjectMocks
	private ChatDomainService chatDomainService;
	private User seller;
	private User buyer;
	private Product product;
	private ChatRoom chatRoom;
	private ChatRoomUser chatRoomUser;

	@BeforeEach
	void setUp() {
		// seller
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

		// buyer
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

		// product
		product = Product.builder()
			.name(PRODUCT_NAME)
			.txMethod(ProductTxMethod.DIRECT)
			.seller(seller)
			.build();
		ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

		// chatRoom
		chatRoom = ChatRoom.builder()
			.product(product)
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);

		chatRoomUser = ChatRoomUser.builder()
			.chatRoom(chatRoom)
			.user(buyer)
			.build();

	}

	private User createUser(Long id, String email, String nickname) {
		User user = User.builder()
			.email(email)
			.nickname(nickname)
			.oAuthType(OAuthType.KAKAO)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(List.of(Address.builder()
				.addressNickname("테스트주소")
				.roadAddress("테스트도로명")
				.detailAddress("테스트상세주소")
				.build()))
			.point(0)
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}


	@Nested
	@DisplayName("기존 채팅방 조회")
	class FindExistingChatRoomTest {

		@Test
		@DisplayName("기존 채팅방 조회 성공")
		void success_findExistingChatRoom() {
			// given
			given(chatRoomUserRepository.findByIdUserIdAndChatRoomProductId(BUYER_ID, PRODUCT_ID))
				.willReturn(Optional.of(chatRoomUser));

			// when
			Optional<ChatRoom> result = chatDomainService.findExistingChatRoom(BUYER_ID, PRODUCT_ID);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().getId()).isEqualTo(CHAT_ROOM_ID);
			verify(chatRoomUserRepository).findByIdUserIdAndChatRoomProductId(BUYER_ID, PRODUCT_ID);
		}

		@Test
		@DisplayName("기존 채팅방 없음")
		void empty_findExistingChatRoom() {
			// given
			given(chatRoomUserRepository.findByIdUserIdAndChatRoomProductId(BUYER_ID, PRODUCT_ID))
				.willReturn(Optional.empty());

			// when
			Optional<ChatRoom> result = chatDomainService.findExistingChatRoom(BUYER_ID, PRODUCT_ID);

			// then
			assertThat(result).isEmpty();
			verify(chatRoomUserRepository).findByIdUserIdAndChatRoomProductId(BUYER_ID, PRODUCT_ID);
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
			given(chatRoomRepository.findAllByUserId(BUYER_ID)).willReturn(chatRoomInfos);

			// when
			List<ChatRoomInfo> result = chatDomainService.findChatRooms(buyer);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).ChatRoomId()).isEqualTo(CHAT_ROOM_ID);
			verify(chatRoomRepository).findAllByUserId(BUYER_ID);
		}

		@Test
		@DisplayName("참여중인 채팅방이 없음")
		void empty_findChatRooms() {
			// given
			given(chatRoomRepository.findAllByUserId(BUYER_ID)).willReturn(List.of());

			// when
			List<ChatRoomInfo> result = chatDomainService.findChatRooms(buyer);

			// then
			assertThat(result).isEmpty();
			verify(chatRoomRepository).findAllByUserId(BUYER_ID);
		}
	}

	@Nested
	@DisplayName("채팅방 접근 권한 검증")
	class ValidateUserAccessTest {

		@Test
		@DisplayName("채팅방 접근 권한 검증 성공")
		void success_validateUserAccess() {
			// given
			given(chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(CHAT_ROOM_ID, BUYER_ID))
				.willReturn(true);

			// when & then
			assertThatCode(() ->
				chatDomainService.validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID))
				.doesNotThrowAnyException();

			verify(chatRoomUserRepository).existsByIdChatRoomIdAndIdUserId(CHAT_ROOM_ID, BUYER_ID);
		}

		@Test
		@DisplayName("채팅방 접근 권한 검증 실패")
		void fail_validateUserAccess_accessDenied() {
			// given
			given(chatRoomUserRepository.existsByIdChatRoomIdAndIdUserId(CHAT_ROOM_ID, BUYER_ID))
				.willReturn(false);

			// when & then
			assertThatThrownBy(() ->
				chatDomainService.validateUserAccessToChatRoom(buyer, CHAT_ROOM_ID))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatRoomUserRepository).existsByIdChatRoomIdAndIdUserId(CHAT_ROOM_ID, BUYER_ID);
		}
	}

	@Nested
	@DisplayName("채팅 기록 조회")
	class FindChatHistoryResponsesTest {

		@Test
		@DisplayName("채팅 기록 조회 성공")
		void success_findChatHistoryResponses() {
			// given
			Long lastId = null;
			int size = 30;
			PageRequest pageRequest = PageRequest.of(0, size);
			ChatHistory history1 = ChatHistory.builder()
				.chatRoom(chatRoom)
				.userId(SELLER_ID)
				.message("안녕하세요")
				.messageType(MessageType.TEXT)
				.sendtime(LocalDateTime.now())
				.build();
			ReflectionTestUtils.setField(history1, "id", 1L);
			ChatHistory history2 = ChatHistory.builder()
				.chatRoom(chatRoom)
				.userId(BUYER_ID)
				.message("반갑습니다")
				.messageType(MessageType.TEXT)
				.sendtime(LocalDateTime.now())
				.build();
			ReflectionTestUtils.setField(history2, "id", 2L);
			List<ChatHistory> chatHistories = List.of(history1, history2);
			Slice<ChatHistory> slice = new SliceImpl<>(chatHistories, pageRequest, false);

			given(chatRoomHistoryRepository.findAllByChatRoomIdOrderBySendTimeAsc(CHAT_ROOM_ID, lastId, pageRequest))
				.willReturn(slice);

			// when
			List<FindChatHistoryResponse> result = chatDomainService.findChatHistoryResponses(CHAT_ROOM_ID, lastId,
				size);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).message()).isEqualTo("안녕하세요");
			assertThat(result.get(1).message()).isEqualTo("반갑습니다");
			verify(chatRoomHistoryRepository).findAllByChatRoomIdOrderBySendTimeAsc(CHAT_ROOM_ID, lastId, pageRequest);
		}

		@Test
		@DisplayName("채팅 기록이 없음")
		void empty_findChatHistoryResponses() {
			// given
			Long lastId = null;
			int size = 30;
			PageRequest pageRequest = PageRequest.of(0, size);
			Slice<ChatHistory> slice = new SliceImpl<>(List.of(), pageRequest, false);

			given(chatRoomHistoryRepository.findAllByChatRoomIdOrderBySendTimeAsc(CHAT_ROOM_ID, lastId, pageRequest))
				.willReturn(slice);

			// when
			List<FindChatHistoryResponse> result = chatDomainService.findChatHistoryResponses(CHAT_ROOM_ID, lastId,
				size);

			// then
			assertThat(result).isEmpty();
			verify(chatRoomHistoryRepository).findAllByChatRoomIdOrderBySendTimeAsc(CHAT_ROOM_ID, lastId, pageRequest);
		}
	}

	@Nested
	@DisplayName("채팅방 생성")
	class CreateChatRoomTest {

		@Test
		@DisplayName("채팅방 생성 성공")
		void success_createChatRoom() {
			// given
			given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);

			// when
			ChatRoom result = chatDomainService.createChatRoom(product, buyer, seller);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getProduct().getId()).isEqualTo(PRODUCT_ID);
			assertThat(result.getChatRoomUsers()).hasSize(2);

			verify(chatRoomRepository).save(any(ChatRoom.class));
		}

		@Test
		@DisplayName("채팅방 생성 실패 - 경매 상품")
		void fail_createChatRoom_auctionProduct() {
			// given
			Product auctionProduct = Product.builder()
				.name(PRODUCT_NAME)
				.txMethod(ProductTxMethod.AUCTION)
				.seller(seller)
				.build();
			ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

			Product product = Product.builder()
				.name(PRODUCT_NAME)
				.txMethod(ProductTxMethod.AUCTION)
				.seller(seller)
				.build();
			ReflectionTestUtils.setField(product, "id", PRODUCT_ID);

			// when & then
			assertThatThrownBy(() ->
				chatDomainService.createChatRoom(auctionProduct, buyer, seller))
				.isInstanceOf(ProductException.class)
				.extracting("errorCode")
				.isEqualTo(ProductErrorCode.INVALID_PRODUCT_TYPE);

			verify(chatRoomRepository, never()).save(any(ChatRoom.class));
		}
	}

	@Nested
	@DisplayName("채팅방 나가기")
	class DeleteUserFromChatRoomTest {

		@Test
		@DisplayName("채팅방 나가기 성공")
		void success_deleteUserFromChatRoom() {
			// given
			given(chatRoomUserRepository.findByIdUserIdAndIdChatRoomId(BUYER_ID, CHAT_ROOM_ID))
				.willReturn(Optional.of(chatRoomUser));
			willDoNothing().given(chatRoomUserRepository).delete(chatRoomUser);

			// when
			String result = chatDomainService.deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID);

			// then
			assertThat(result).isEqualTo(BUYER_EMAIL);
			verify(chatRoomUserRepository).findByIdUserIdAndIdChatRoomId(BUYER_ID, CHAT_ROOM_ID);
			verify(chatRoomUserRepository).delete(chatRoomUser);
		}

		@Test
		@DisplayName("채팅방 나가기 실패 - 접근 권한 없음")
		void fail_deleteUserFromChatRoom_accessDenied() {
			// given
			given(chatRoomUserRepository.findByIdUserIdAndIdChatRoomId(BUYER_ID, CHAT_ROOM_ID))
				.willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() ->
				chatDomainService.deleteUserFromChatRoom(BUYER_ID, CHAT_ROOM_ID))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatRoomUserRepository).findByIdUserIdAndIdChatRoomId(BUYER_ID, CHAT_ROOM_ID);
			verify(chatRoomUserRepository, never()).delete(any(ChatRoomUser.class));
		}
	}

	@Nested
	@DisplayName("채팅 기록 저장")
	class SaveChatHistoriesTest {

		@Test
		@DisplayName("채팅 기록 저장 성공")
		void success_saveChatHistories() {
			// given
			List<ChatMessageInfo> messagesFromRedis = List.of(
				ChatMessageInfo.of(CHAT_ROOM_ID, SELLER_ID, SELLER_EMAIL, "안녕하세요", MessageType.TEXT,
					LocalDateTime.now()),
				ChatMessageInfo.of(CHAT_ROOM_ID, BUYER_ID, BUYER_EMAIL, "반갑습니다", MessageType.TEXT, LocalDateTime.now())
			);
			given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));
			given(chatRoomHistoryRepository.saveAll(any(List.class))).willReturn(List.of());

			// when
			chatDomainService.saveChatHistories(CHAT_ROOM_ID, messagesFromRedis);

			// then
			verify(chatRoomRepository).findById(CHAT_ROOM_ID);
			verify(chatRoomHistoryRepository).saveAll(any(List.class));
		}

		@Test
		@DisplayName("채팅 기록 저장 실패 - 채팅방 없음")
		void fail_saveChatHistories_chatRoomNotFound() {
			// given
			List<ChatMessageInfo> messagesFromRedis = List.of(
				ChatMessageInfo.of(CHAT_ROOM_ID, SELLER_ID, SELLER_EMAIL, "안녕하세요", MessageType.TEXT,
					LocalDateTime.now()),
				ChatMessageInfo.of(CHAT_ROOM_ID, BUYER_ID, BUYER_EMAIL, "반갑습니다", MessageType.TEXT, LocalDateTime.now())
			);
			given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() ->
				chatDomainService.saveChatHistories(CHAT_ROOM_ID, messagesFromRedis))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND);

			verify(chatRoomRepository).findById(CHAT_ROOM_ID);
			verify(chatRoomHistoryRepository, never()).saveAll(any(List.class));
		}
	}

	@Nested
	@DisplayName("채팅방 찾기")
	class FindChatRoomTest {

		@Test
		@DisplayName("채팅방 찾기 성공")
		void success_findChatRoom() {
			// given
			given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));

			// when
			ChatRoom result = chatDomainService.findChatRoom(CHAT_ROOM_ID);

			// then
			assertThat(result).isNotNull();
			assertThat(result.getId()).isEqualTo(CHAT_ROOM_ID);
			verify(chatRoomRepository).findById(CHAT_ROOM_ID);
		}

		@Test
		@DisplayName("채팅방 찾기 실패 - 채팅방 없음")
		void fail_findChatRoom_notFound() {
			// given
			given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.empty());

			// when & then
			assertThatThrownBy(() ->
				chatDomainService.findChatRoom(CHAT_ROOM_ID))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND);

			verify(chatRoomRepository).findById(CHAT_ROOM_ID);
		}
	}
}
