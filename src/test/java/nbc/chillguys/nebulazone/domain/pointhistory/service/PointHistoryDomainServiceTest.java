package nbc.chillguys.nebulazone.domain.pointhistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.application.pointhistory.dto.request.PointRequest;
import nbc.chillguys.nebulazone.domain.pointhistory.dto.PointHistoryCommand;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryStatus;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;
import nbc.chillguys.nebulazone.domain.user.entity.User;

@ExtendWith(MockitoExtension.class)
class PointHistoryDomainServiceTest {

	@Mock
	private PointHistoryRepository pointHistoryRepository;
	@InjectMocks
	private PointHistoryDomainService pointHistoryDomainService;

	@Nested
	@DisplayName("포인트 내역 생성")
	class CreatePointHistoryTest {
		@Test
		void success_createPointHistory() {
			// given
			User user = mock(User.class);

			PointRequest pointRequest = new PointRequest(1000L, PointHistoryType.CHARGE, "123-456-789");
			PointHistoryCommand command = PointHistoryCommand.of(pointRequest, user);

			// when
			pointHistoryDomainService.createPointHistory(command, PointHistoryStatus.PENDING);

			// then
			verify(pointHistoryRepository).save(any(PointHistory.class));

		}
	}

	@Nested
	@DisplayName("포인트 내역 조회")
	class FindPointHistoryTest {
		@Test
		@DisplayName("특정 유저의 PENDING 내역 조회")
		void success_findPointHistoriesByUserAndStatus() {
			// given
			Long userId = 1L;
			PointHistoryStatus status = PointHistoryStatus.PENDING;
			PointHistory ph1 = mock(PointHistory.class);

			given(pointHistoryRepository.findByUserIdAndPointHistoryStatus(userId, status))
				.willReturn(List.of(ph1));

			// when
			List<PointHistory> result = pointHistoryDomainService.findPointHistoriesByUserAndStatus(userId, status);

			// then
			assertThat(result).hasSize(1);
			verify(pointHistoryRepository).findByUserIdAndPointHistoryStatus(userId, status);
		}

		@Test
		@DisplayName("상태 파라미터 없으면 전체 조회")
		void success_findPointHistoriesByUser_noStatus() {
			// given
			Long userId = 1L;
			PointHistory ph1 = mock(PointHistory.class);

			given(pointHistoryRepository.findByUserId(userId)).willReturn(List.of(ph1));

			// when
			List<PointHistory> result = pointHistoryDomainService.findPointHistoriesByUserAndStatus(userId, null);

			// then
			assertThat(result).hasSize(1);
			verify(pointHistoryRepository).findByUserId(userId);
		}
	}

	@Nested
	@DisplayName("포인트 요청 취소")
	class RejectPointRequestTest {
		@Test
		@DisplayName("성공: 취소 처리 및 상태/소유자 검증")
		void success_rejectPointRequest() {
			// given
			User user = mock(User.class);
			given(user.getId()).willReturn(1L);
			PointHistory pointHistory = mock(PointHistory.class);
			given(pointHistory.getUser()).willReturn(user);
			given(pointHistory.getPointHistoryStatus()).willReturn(PointHistoryStatus.PENDING);

			// when
			pointHistoryDomainService.rejectPointRequest(pointHistory, 1L);

			// then
			verify(pointHistory).reject();
		}
	}

	@Nested
	@DisplayName("내역 단건/페이징 조회")
	class FindActiveAndPagingTest {
		@Test
		@DisplayName("성공: 활성 내역 ID 조회")
		void success_findActivePointHistory() {
			// given
			Long id = 2L;
			PointHistory pointHistory = mock(PointHistory.class);

			given(pointHistoryRepository.findActivePointHistoryById(id))
				.willReturn(Optional.of(pointHistory));

			// when
			PointHistory result = pointHistoryDomainService.findActivePointHistory(id);

			// then
			assertThat(result).isEqualTo(pointHistory);
		}

		@Test
		@DisplayName("포인트 내역 페이징 조회 성공")
		void success_findPointHistoriesByUser() {
			// given
			Long userId = 1L;
			Pageable pageable = PageRequest.of(0, 2);
			PointHistory point1 = mock(PointHistory.class);
			Page<PointHistory> mockPage = new PageImpl<>(List.of(point1));
			given(pointHistoryRepository.findByUserId(userId, pageable)).willReturn(mockPage);

			// when
			Page<PointHistory> result = pointHistoryDomainService.findPointHistoriesByUser(userId, pageable);

			// then
			assertThat(result.getContent()).hasSize(1);
		}
	}
}
