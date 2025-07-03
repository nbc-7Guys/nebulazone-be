package nbc.chillguys.nebulazone.application.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import nbc.chillguys.nebulazone.application.chat.dto.request.ImageMessageRequest;
import nbc.chillguys.nebulazone.domain.chat.dto.request.ChatSendTextMessageCommand;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatMessageInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatErrorCode;
import nbc.chillguys.nebulazone.domain.chat.exception.ChatException;
import nbc.chillguys.nebulazone.domain.chat.service.ChatDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.Address;
import nbc.chillguys.nebulazone.domain.user.entity.OAuthType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.entity.UserRole;
import nbc.chillguys.nebulazone.infra.gcs.client.GcsClient;
import nbc.chillguys.nebulazone.infra.redis.publisher.RedisMessagePublisher;
import nbc.chillguys.nebulazone.infra.redis.service.WebSocketSessionRedisService;
import nbc.chillguys.nebulazone.infra.websocket.dto.SessionUser;

@DisplayName("채팅 메시지 서비스 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

	// 테스트 상수
	private static final String SESSION_ID = "test-session-id";
	private static final String USER_EMAIL = "test@test.com";
	private static final Long USER_ID = 1L;
	private static final Long ROOM_ID = 1L;
	private static final String MESSAGE_CONTENT = "안녕하세요";
	private static final String IMAGE_URL = "https://test-image-url.com/image.jpg";
	@Mock
	private ChatMessageRedisService chatMessageRedisService;
	@Mock
	private GcsClient gcsClient;
	@Mock
	private RedisMessagePublisher redisMessagePublisher;
	@Mock
	private ChatDomainService chatDomainService;
	@Mock
	private WebSocketSessionRedisService webSocketSessionRedisService;
	@InjectMocks
	private ChatMessageService chatMessageService;
	// 공통 테스트 픽스처
	private User user;
	private SessionUser sessionUser;
	private ChatSendTextMessageCommand textMessageCommand;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.email(USER_EMAIL)
			.nickname("테스트유저")
			.oAuthType(OAuthType.KAKAO)
			.roles(Set.of(UserRole.ROLE_USER))
			.addresses(List.of(Address.builder()
				.addressNickname("테스트주소")
				.roadAddress("테스트도로명")
				.detailAddress("테스트상세주소")
				.build()))
			.point(0)
			.build();
		ReflectionTestUtils.setField(user, "id", USER_ID);
		sessionUser = SessionUser.from(user);
		textMessageCommand = new ChatSendTextMessageCommand(MESSAGE_CONTENT, "TEXT");
	}

	@Nested
	@DisplayName("텍스트 메시지 전송")
	class SendTextMessageTest {

		@Test
		@DisplayName("텍스트 메시지 전송 성공")
		void success_sendTextMessage() {
			// given
			given(webSocketSessionRedisService.getUserIdBySessionId(SESSION_ID)).willReturn(sessionUser);
			given(webSocketSessionRedisService.getRoomIdBySessionId(SESSION_ID)).willReturn(ROOM_ID);
			willDoNothing().given(redisMessagePublisher).publishChatMessage(eq(ROOM_ID), any(ChatMessageInfo.class));
			willDoNothing().given(chatMessageRedisService).saveMessageToRedis(eq(ROOM_ID), any(ChatMessageInfo.class));

			// when
			chatMessageService.sendTextMessage(SESSION_ID, ROOM_ID, textMessageCommand);

			// then
			verify(webSocketSessionRedisService).getUserIdBySessionId(SESSION_ID);
			verify(webSocketSessionRedisService).getRoomIdBySessionId(SESSION_ID);
			verify(redisMessagePublisher).publishChatMessage(eq(ROOM_ID), any(ChatMessageInfo.class));
			verify(chatMessageRedisService).saveMessageToRedis(eq(ROOM_ID), any(ChatMessageInfo.class));
		}

		@Test
		@DisplayName("텍스트 메시지 전송 실패 - 세션 사용자 없음")
		void fail_sendTextMessage_noSessionUser() {
			// given
			SessionUser invalidSessionUser = new SessionUser(null, null);
			given(webSocketSessionRedisService.getUserIdBySessionId(SESSION_ID)).willReturn(invalidSessionUser);
			given(webSocketSessionRedisService.getRoomIdBySessionId(SESSION_ID)).willReturn(ROOM_ID);

			// when & then
			assertThatThrownBy(() ->
				chatMessageService.sendTextMessage(SESSION_ID, ROOM_ID, textMessageCommand))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(webSocketSessionRedisService).getUserIdBySessionId(SESSION_ID);
			verify(webSocketSessionRedisService).getRoomIdBySessionId(SESSION_ID);
			verify(redisMessagePublisher, never()).publishChatMessage(any(), any());
			verify(chatMessageRedisService, never()).saveMessageToRedis(any(), any());
		}

		@Test
		@DisplayName("텍스트 메시지 전송 실패 - 방 ID 불일치")
		void fail_sendTextMessage_roomIdMismatch() {
			// given
			Long differentRoomId = 999L;
			given(webSocketSessionRedisService.getUserIdBySessionId(SESSION_ID)).willReturn(sessionUser);
			given(webSocketSessionRedisService.getRoomIdBySessionId(SESSION_ID)).willReturn(differentRoomId);

			// when & then
			assertThatThrownBy(() ->
				chatMessageService.sendTextMessage(SESSION_ID, ROOM_ID, textMessageCommand))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(webSocketSessionRedisService).getUserIdBySessionId(SESSION_ID);
			verify(webSocketSessionRedisService).getRoomIdBySessionId(SESSION_ID);
			verify(redisMessagePublisher, never()).publishChatMessage(any(), any());
			verify(chatMessageRedisService, never()).saveMessageToRedis(any(), any());
		}
	}

	@Nested
	@DisplayName("이미지 메시지 전송")
	class SendImageMessageTest {

		@Test
		@DisplayName("이미지 메시지 전송 성공")
		void success_sendImageMessage() {
			// given
			MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
				"test image content".getBytes());
			ImageMessageRequest image = new ImageMessageRequest(imageFile, "IMAGE");

			willDoNothing().given(chatDomainService).validateUserAccessToChatRoom(user, ROOM_ID);
			given(gcsClient.uploadFile(imageFile)).willReturn(IMAGE_URL);
			willDoNothing().given(redisMessagePublisher).publishChatMessage(eq(ROOM_ID), any(ChatMessageInfo.class));
			willDoNothing().given(chatMessageRedisService).saveMessageToRedis(eq(ROOM_ID), any(ChatMessageInfo.class));

			// when
			chatMessageService.sendImageMessage(user, ROOM_ID, image);

			// then
			verify(chatDomainService).validateUserAccessToChatRoom(user, ROOM_ID);
			verify(gcsClient).uploadFile(imageFile);
			verify(redisMessagePublisher).publishChatMessage(eq(ROOM_ID), any(ChatMessageInfo.class));
			verify(chatMessageRedisService).saveMessageToRedis(eq(ROOM_ID), any(ChatMessageInfo.class));
		}

		@Test
		@DisplayName("이미지 메시지 전송 실패 - 접근 권한 없음")
		void fail_sendImageMessage_accessDenied() {
			// given
			MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
				"test image content".getBytes());
			ImageMessageRequest image = new ImageMessageRequest(imageFile, "IMAGE");

			willThrow(new ChatException(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED))
				.given(chatDomainService).validateUserAccessToChatRoom(user, ROOM_ID);

			// when & then
			assertThatThrownBy(() ->
				chatMessageService.sendImageMessage(user, ROOM_ID, image))
				.isInstanceOf(ChatException.class)
				.extracting("errorCode")
				.isEqualTo(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);

			verify(chatDomainService).validateUserAccessToChatRoom(user, ROOM_ID);
			verify(gcsClient, never()).uploadFile(any());
			verify(redisMessagePublisher, never()).publishChatMessage(any(), any());
			verify(chatMessageRedisService, never()).saveMessageToRedis(any(), any());
		}
	}

	@Nested
	@DisplayName("Redis 메시지를 DB에 저장")
	class SaveMessagesToDbTest {

		@Test
		@DisplayName("Redis 메시지 DB 저장 성공")
		void success_saveMessagesToDb() {
			// given
			List<ChatMessageInfo> messagesFromRedis = List.of(
				ChatMessageInfo.of(ROOM_ID, USER_ID, USER_EMAIL, "첫 번째 메시지", MessageType.TEXT, LocalDateTime.now()),
				ChatMessageInfo.of(ROOM_ID, USER_ID, USER_EMAIL, "두 번째 메시지", MessageType.TEXT, LocalDateTime.now())
			);
			given(chatMessageRedisService.getMessagesFromRedis(ROOM_ID)).willReturn(messagesFromRedis);
			willDoNothing().given(chatDomainService).saveChatHistories(ROOM_ID, messagesFromRedis);
			willDoNothing().given(chatMessageRedisService).deleteMessagesInRedis(ROOM_ID);

			// when
			chatMessageService.saveMessagesToDb(ROOM_ID);

			// then
			verify(chatMessageRedisService).getMessagesFromRedis(ROOM_ID);
			verify(chatDomainService).saveChatHistories(ROOM_ID, messagesFromRedis);
			verify(chatMessageRedisService).deleteMessagesInRedis(ROOM_ID);
		}

		@Test
		@DisplayName("Redis에 메시지가 없어서 저장하지 않음")
		void skip_saveMessagesToDb_emptyMessages() {
			// given
			given(chatMessageRedisService.getMessagesFromRedis(ROOM_ID)).willReturn(List.of());

			// when
			chatMessageService.saveMessagesToDb(ROOM_ID);

			// then
			verify(chatMessageRedisService).getMessagesFromRedis(ROOM_ID);
			verify(chatDomainService, never()).saveChatHistories(any(), any());
			verify(chatMessageRedisService, never()).deleteMessagesInRedis(any());
		}
	}
}
