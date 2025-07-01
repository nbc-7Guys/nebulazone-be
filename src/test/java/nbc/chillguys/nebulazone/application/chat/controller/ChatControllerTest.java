package nbc.chillguys.nebulazone.application.chat.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.chat.dto.request.CreateChatRoomRequest;
import nbc.chillguys.nebulazone.application.chat.dto.response.CreateChatRoomResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatHistoryResponse;
import nbc.chillguys.nebulazone.application.chat.dto.response.FindChatRoomResponses;
import nbc.chillguys.nebulazone.application.chat.service.ChatService;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.chat.dto.response.ChatRoomInfo;
import nbc.chillguys.nebulazone.domain.chat.entity.MessageType;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.MockMvc.TestMockConfig;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@Import({TestSecurityConfig.class, TestMockConfig.class})
@DisplayName("채팅 컨트롤러 단위 테스트")
@WebMvcTest(
	controllers = ChatController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			JwtAuthenticationFilter.class
		})
	}
)
class ChatControllerTest {

	private static final Long PRODUCT_ID = 1L;
	private static final Long CHAT_ROOM_ID = 1L;
	@MockitoBean
	private ChatService chatService;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Nested
	@DisplayName("채팅방 생성 테스트")
	class CreateChatRoomTest {

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("채팅방 생성 성공")
		void createChatRoom_Success() throws Exception {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);
			CreateChatRoomResponse response = new CreateChatRoomResponse(
				CHAT_ROOM_ID, PRODUCT_ID, 1L, "테스트 상품", 10000L, 2L, "판매자");

			given(chatService.getOrCreate(any(User.class), eq(request)))
				.willReturn(response);

			// when & then
			mockMvc.perform(post("/chat/rooms")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.chatRoomId").value(CHAT_ROOM_ID))
				.andExpect(jsonPath("$.productName").value("테스트 상품"));

			verify(chatService).getOrCreate(any(User.class), eq(request));
		}

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("채팅방 생성 시 서비스 예외 발생")
		void createChatRoom_ServiceException() throws Exception {
			// given
			CreateChatRoomRequest request = new CreateChatRoomRequest(PRODUCT_ID);

			given(chatService.getOrCreate(any(User.class), eq(request)))
				.willThrow(new RuntimeException("서비스 에러"));

			// when & then
			mockMvc.perform(post("/chat/rooms")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isInternalServerError());
		}
	}

	@Nested
	@DisplayName("채팅방 목록 조회 테스트")
	class FindChatRoomsTest {

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("참여중인 채팅방 목록 조회 성공")
		void findChatRooms_Success() throws Exception {
			// given
			List<ChatRoomInfo> chatRoomInfos = List.of(
				new ChatRoomInfo("테스트 상품1", "판매자1", 1L, 1L, 1L, 10000L, false, "image-url-1"),
				new ChatRoomInfo("테스트 상품2", "판매자2", 2L, 2L, 2L, 20000L, false, "image-url-2")
			);
			FindChatRoomResponses response = FindChatRoomResponses.of(chatRoomInfos);

			given(chatService.findChatRooms(any(User.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/chat/rooms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.chatRooms").isArray())
				.andExpect(jsonPath("$.chatRooms[0].productName").value("테스트 상품1"))
				.andExpect(jsonPath("$.chatRooms[1].productName").value("테스트 상품2"));

			verify(chatService).findChatRooms(any(User.class));
		}

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("참여중인 채팅방이 없는 경우")
		void findChatRooms_Empty() throws Exception {
			// given
			FindChatRoomResponses response = FindChatRoomResponses.of(List.of());

			given(chatService.findChatRooms(any(User.class)))
				.willReturn(response);

			// when & then
			mockMvc.perform(get("/chat/rooms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.chatRooms").isArray())
				.andExpect(jsonPath("$.chatRooms").isEmpty());

			verify(chatService).findChatRooms(any(User.class));
		}
	}

	@Nested
	@DisplayName("채팅 기록 조회 테스트")
	class FindChatHistoriesTest {

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("채팅 기록 조회 성공")
		void findChatHistories_Success() throws Exception {
			// given
			List<FindChatHistoryResponse> responses = List.of(
				new FindChatHistoryResponse(1L, "안녕하세요", LocalDateTime.now(), MessageType.TEXT),
				new FindChatHistoryResponse(2L, "반갑습니다", LocalDateTime.now(), MessageType.TEXT)
			);

			given(chatService.findChatHistories(any(User.class), eq(CHAT_ROOM_ID), eq(null), eq(30)))
				.willReturn(responses);

			// when & then
			mockMvc.perform(get("/chat/rooms/history/{roomId}", CHAT_ROOM_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].message").value("안녕하세요"))
				.andExpect(jsonPath("$[1].message").value("반갑습니다"));

			verify(chatService).findChatHistories(any(User.class), eq(CHAT_ROOM_ID), eq(null), eq(30));
		}

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("페이징 파라미터와 함께 채팅 기록 조회")
		void findChatHistories_WithPaging() throws Exception {
			// given
			Long lastId = 10L;
			int size = 20;
			List<FindChatHistoryResponse> responses = List.of(
				new FindChatHistoryResponse(11L, "페이징 메시지", LocalDateTime.now(), MessageType.TEXT)
			);

			given(chatService.findChatHistories(any(User.class), eq(CHAT_ROOM_ID), eq(lastId), eq(size)))
				.willReturn(responses);

			// when & then
			mockMvc.perform(get("/chat/rooms/history/{roomId}", CHAT_ROOM_ID)
					.param("lastId", lastId.toString())
					.param("size", String.valueOf(size)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$[0].message").value("페이징 메시지"));

			verify(chatService).findChatHistories(any(User.class), eq(CHAT_ROOM_ID), eq(lastId), eq(size));
		}
	}

	@Nested
	@DisplayName("채팅방 나가기 테스트")
	class LeaveChatRoomTest {

		@Test
		@WithCustomMockUser(id = 1L)
		@DisplayName("채팅방 나가기 성공")
		void leaveChatRoom_Success() throws Exception {
			// given
			doNothing().when(chatService).exitChatRoom(any(User.class), eq(CHAT_ROOM_ID));

			// when & then
			mockMvc.perform(delete("/chat/rooms/{roomId}", CHAT_ROOM_ID))
				.andExpect(status().isOk())
				.andExpect(content().string("성공적으로 채팅방을 나갔습니다."));

			verify(chatService).exitChatRoom(any(User.class), eq(CHAT_ROOM_ID));
		}
	}
}
