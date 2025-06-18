package nbc.chillguys.nebulazone.application.pointhistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointHistoryResponse;
import nbc.chillguys.nebulazone.application.pointhistory.dto.response.PointResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryDomainService;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

	@Mock
	private PointHistoryDomainService pointHistoryDomainService;
	@Mock
	private UserDomainService userDomainService;
	@InjectMocks
	private PointHistoryService pointHistoryService;

	@Nested
	@DisplayName("포인트 내역 생성 테스트")
	class CreatePointHistoryTest {
		@Test
		@DisplayName("CHARGE 타입 포인트 내역 생성 성공")
		void success_createChargePointHistory() {
			// given
			Long userId = 1L;
			User mockUser = mock(User.class);
			PointRequest req = new PointRequest(1000L, PointHistoryType.CHARGE, "123-456-789");
			PointHistory mockPointHistory = mock(PointHistory.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(pointHistoryDomainService.createPointHistory(any(PointHistoryCommand.class))).willReturn(
				mockPointHistory);
			given(mockPointHistory.getId()).willReturn(1L);
			given(mockPointHistory.getPrice()).willReturn(1000L);
			given(mockPointHistory.getPointHistoryType()).willReturn(PointHistoryType.CHARGE);
			given(mockPointHistory.getPointHistoryStatus()).willReturn(PointHistoryStatus.PENDING);
			given(mockPointHistory.getCreatedAt()).willReturn(LocalDateTime.now());

			// when
			PointResponse response = pointHistoryService.createPointHistory(req, userId);

			// then
			assertThat(response.price()).isEqualTo(1000);
			assertThat(response.type()).isEqualTo(PointHistoryType.CHARGE);
		}

		@Test
		@DisplayName("EXCHANGE 타입은 포인트 검증 호출")
		void createExchangePointHistory_validatesPoint() {
			// given
			Long userId = 1L;
			User mockUser = mock(User.class);
			PointRequest req = new PointRequest(3000L, PointHistoryType.EXCHANGE, "321-654-987");
			PointHistory mockPointHistory = mock(PointHistory.class);

			given(userDomainService.findActiveUserById(userId)).willReturn(mockUser);
			given(pointHistoryDomainService.createPointHistory(any(PointHistoryCommand.class))).willReturn(
				mockPointHistory);

			// when
			pointHistoryService.createPointHistory(req, userId);

			// then
			verify(userDomainService).validEnoughPoint(mockUser, 3000L);
		}
	}

	@Nested
	@DisplayName("포인트 요청/내역 조회 테스트")
	class FindTests {
		@Test
		@DisplayName("포인트 요청 리스트 조회 성공")
		void success_findMyPointRequests() {
			// given
			Long userId = 1L;
			PointHistoryStatus status = PointHistoryStatus.PENDING;
			PointHistory point1 = mock(PointHistory.class);
			given(point1.getPrice()).willReturn(1000L);
			given(point1.getAccount()).willReturn("123-456-789");
			given(point1.getPointHistoryType()).willReturn(PointHistoryType.CHARGE);
			given(point1.getPointHistoryStatus()).willReturn(PointHistoryStatus.PENDING);
			given(point1.getCreatedAt()).willReturn(LocalDateTime.now());

			given(pointHistoryDomainService.findPointHistoriesByUserAndStatus(userId, status)).willReturn(
				List.of(point1));

			// when
			List<PointHistoryResponse> result = pointHistoryService.findMyPointRequests(userId, status);

			// then
			assertThat(result).hasSize(1);
			assertThat(result.getFirst().price()).isEqualTo(1000);
		}

		@Test
		@DisplayName("포인트 내역 페이징 조회 성공")
		void success_findMyPointHistories() {
			// given
			Long userId = 1L;
			int page = 1;
			int size = 2;
			PointHistory point1 = mock(PointHistory.class);
			PointHistory point2 = mock(PointHistory.class);
			given(point1.getPrice()).willReturn(1000L);
			given(point2.getPrice()).willReturn(500L);
			given(point1.getPointHistoryType()).willReturn(PointHistoryType.CHARGE);
			given(point2.getPointHistoryType()).willReturn(PointHistoryType.EXCHANGE);

			Page<PointHistory> pageMock = new PageImpl<>(List.of(point1, point2));
			given(pointHistoryDomainService.findPointHistoriesByUser(eq(userId), any(Pageable.class))).willReturn(
				pageMock);

			// when
			CommonPageResponse<PointHistoryResponse> result = pointHistoryService.findMyPointHistories(userId, page,
				size);

			// then
			assertThat(result.content()).hasSize(2);
			assertThat(result.content().get(0).price()).isEqualTo(1000);
			assertThat(result.content().get(1).type()).isEqualTo(PointHistoryType.EXCHANGE);
		}
	}

	@Nested
	@DisplayName("포인트 요청 취소 테스트")
	class RejectPointRequestTest {
		@Test
		@DisplayName("요청 취소")
		void success_rejectPointRequest() {
			// given
			Long userId = 1L;
			Long pointHistoryId = 77L;
			PointHistory mockHistory = mock(PointHistory.class);

			given(pointHistoryDomainService.findActivePointHistory(pointHistoryId)).willReturn(mockHistory);
			willDoNothing().given(pointHistoryDomainService).rejectPointRequest(mockHistory, userId);

			// when
			pointHistoryService.rejectPointRequest(userId, pointHistoryId);

			// then
			verify(pointHistoryDomainService, times(1)).rejectPointRequest(mockHistory, userId);
		}
	}
}
