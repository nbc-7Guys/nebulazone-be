package nbc.chillguys.nebulazone.application.chat.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import nbc.chillguys.nebulazone.application.chat.dto.request.ImageMessageRequest;
import nbc.chillguys.nebulazone.application.chat.service.ChatMessageService;
import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@Import({TestSecurityConfig.class, TestMockConfig.class})
@DisplayName("채팅 메시지 컨트롤러 단위 테스트")
@WebMvcTest(
	controllers = ChatMessageController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			JwtAuthenticationFilter.class
		})
	}
)
class ChatMessageControllerTest {

	private static final Long CHAT_ROOM_ID = 1L;
	@MockitoBean
	private ChatMessageService chatMessageService;
	@Autowired
	private MockMvc mockMvc;

	@Nested
	@DisplayName("이미지 메시지 전송 테스트")
	class SendImageMessageTest {

		@Test
		@WithCustomMockUser
		@DisplayName("이미지 메시지 전송 성공")
		void sendImageMessage_Success() throws Exception {
			// given
			MockMultipartFile imageFile = new MockMultipartFile(
				"image",
				"test-image.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes()
			);

			doNothing().when(chatMessageService)
				.sendImageMessage(any(User.class), eq(CHAT_ROOM_ID), any(ImageMessageRequest.class));

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.file(imageFile)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk());

			verify(chatMessageService).sendImageMessage(any(User.class), eq(CHAT_ROOM_ID),
				any(ImageMessageRequest.class));
		}

		@Test
		@WithCustomMockUser
		@DisplayName("PNG 이미지 파일 전송 성공")
		void sendImageMessage_PngFile_Success() throws Exception {
			// given
			MockMultipartFile imageFile = new MockMultipartFile(
				"image",
				"test-image.png",
				MediaType.IMAGE_PNG_VALUE,
				"test png image content".getBytes()
			);

			doNothing().when(chatMessageService)
				.sendImageMessage(any(User.class), eq(CHAT_ROOM_ID), any(ImageMessageRequest.class));

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.file(imageFile)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk());

			verify(chatMessageService).sendImageMessage(any(User.class), eq(CHAT_ROOM_ID),
				any(ImageMessageRequest.class));
		}

		@Test
		@DisplayName("이미지 파일 없이 요청 - 400 에러")
		void sendImageMessage_NoImageFile() throws Exception {
			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isBadRequest());
		}

		@Test
		@WithCustomMockUser
		@DisplayName("빈 이미지 파일로 요청 - 200 성공 (현재 구현에서는 빈 파일도 허용)")
		void sendImageMessage_EmptyImageFile() throws Exception {
			// given
			MockMultipartFile emptyImageFile = new MockMultipartFile(
				"image",
				"empty.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				new byte[0]
			);

			doNothing().when(chatMessageService)
				.sendImageMessage(any(User.class), eq(CHAT_ROOM_ID), any(ImageMessageRequest.class));

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.file(emptyImageFile)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isOk());

			verify(chatMessageService).sendImageMessage(any(User.class), eq(CHAT_ROOM_ID),
				any(ImageMessageRequest.class));
		}

		@Test
		@WithCustomMockUser
		@DisplayName("타입 파라미터 없이 요청 - 400 에러")
		void sendImageMessage_NoTypeParameter() throws Exception {
			// given
			MockMultipartFile imageFile = new MockMultipartFile(
				"image",
				"test-image.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes()
			);

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.file(imageFile)
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isBadRequest());
		}

		@Test
		@WithCustomMockUser
		@DisplayName("잘못된 채팅방 ID 형식 - 500 에러")
		void sendImageMessage_InvalidRoomIdFormat() throws Exception {
			// given
			MockMultipartFile imageFile = new MockMultipartFile(
				"image",
				"test-image.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes()
			);

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", "invalid-id")
					.file(imageFile)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isInternalServerError());
		}

		@Test
		@WithCustomMockUser
		@DisplayName("서비스 예외 발생 시 500 에러")
		void sendImageMessage_ServiceException() throws Exception {
			// given
			MockMultipartFile imageFile = new MockMultipartFile(
				"image",
				"test-image.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"test image content".getBytes()
			);

			doThrow(new RuntimeException("서비스 에러"))
				.when(chatMessageService)
				.sendImageMessage(any(User.class), eq(CHAT_ROOM_ID), any(ImageMessageRequest.class));

			// when & then
			mockMvc.perform(multipart("/send/image/{roomId}", CHAT_ROOM_ID)
					.file(imageFile)
					.param("type", "IMAGE")
					.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(status().isInternalServerError());
		}
	}
}
