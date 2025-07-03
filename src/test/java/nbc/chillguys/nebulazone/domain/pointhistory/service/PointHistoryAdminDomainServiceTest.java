package nbc.chillguys.nebulazone.domain.pointhistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.repository.PointHistoryRepository;

@ExtendWith(MockitoExtension.class)
class PointHistoryAdminDomainServiceTest {

	@Mock
	private PointHistoryRepository pointHistoryRepository;

	@InjectMocks
	private PointHistoryAdminDomainService pointHistoryAdminDomainService;

	@Nested
	@DisplayName("포인트 요청 승인")
	class ApprovePointHistoryTest {

		@Test
		@DisplayName("성공: 포인트 요청 승인 시 상태 변경")
		void success_approvePointHistory() {
			// given
			Long pointHistoryId = 1L;
			PointHistory pointHistory = mock(PointHistory.class);

			given(pointHistoryRepository.findActivePointHistoryById(pointHistoryId))
				.willReturn(Optional.of(pointHistory));

			// when
			PointHistory result = pointHistoryAdminDomainService.approvePointHistory(pointHistoryId);

			// then
			verify(pointHistory).approve();
			assertThat(result).isEqualTo(pointHistory);
		}
	}

	@Nested
	@DisplayName("포인트 요청 거절")
	class RejectPointHistoryTest {

		@Test
		@DisplayName("성공: 포인트 요청 거절 시 상태 변경")
		void success_rejectPointHistory() {
			// given
			Long pointHistoryId = 1L;
			PointHistory pointHistory = mock(PointHistory.class);

			given(pointHistoryRepository.findActivePointHistoryById(pointHistoryId))
				.willReturn(Optional.of(pointHistory));

			// when
			pointHistoryAdminDomainService.rejectPointHistory(pointHistoryId);

			// then
			verify(pointHistory).reject();
		}
	}
}
