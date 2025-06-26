package nbc.chillguys.nebulazone.application.pointhistory.service;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistory;
import nbc.chillguys.nebulazone.domain.pointhistory.entity.PointHistoryType;
import nbc.chillguys.nebulazone.domain.pointhistory.service.PointHistoryAdminDomainService;
import nbc.chillguys.nebulazone.domain.user.dto.UserPointChargeCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;

@ExtendWith(MockitoExtension.class)
class PointHistoryAdminServiceTest {

    @Mock
    private PointHistoryAdminDomainService pointHistoryAdminDomainService;

    @Mock
    private UserDomainService userDomainService;

    @InjectMocks
    private PointHistoryAdminService pointHistoryAdminService;

    @Nested
    @DisplayName("포인트 요청 승인")
    class ApprovePointHistoryTest {

        @Test
        @DisplayName("성공: CHARGE 타입 포인트 요청 승인 시 상태 변경 및 포인트 충전")
        void success_approvePointHistory_chargeType() {
            // given
            Long pointHistoryId = 1L;
            Long userId = 2L;
            Long price = 1000L;

            User user = mock(User.class);
            given(user.getId()).willReturn(userId);

            PointHistory pointHistory = mock(PointHistory.class);
            given(pointHistory.getPointHistoryType()).willReturn(PointHistoryType.CHARGE);
            given(pointHistory.getUser()).willReturn(user);
            given(pointHistory.getPrice()).willReturn(price);

            given(pointHistoryAdminDomainService.approvePointHistory(pointHistoryId))
                .willReturn(pointHistory);

            // when
            pointHistoryAdminService.approvePointHistory(pointHistoryId);

            // then
            verify(pointHistoryAdminDomainService).approvePointHistory(pointHistoryId);
            verify(userDomainService).chargeUserPoint(any(UserPointChargeCommand.class));
        }

        @Test
        @DisplayName("성공: CHARGE 타입이 아닌 포인트 요청 승인 시 상태만 변경")
        void success_approvePointHistory_nonChargeType() {
            // given
            Long pointHistoryId = 1L;

            PointHistory pointHistory = mock(PointHistory.class);
            given(pointHistory.getPointHistoryType()).willReturn(PointHistoryType.EXCHANGE);

            given(pointHistoryAdminDomainService.approvePointHistory(pointHistoryId))
                .willReturn(pointHistory);

            // when
            pointHistoryAdminService.approvePointHistory(pointHistoryId);

            // then
            verify(pointHistoryAdminDomainService).approvePointHistory(pointHistoryId);
            verify(userDomainService, never()).chargeUserPoint(any(UserPointChargeCommand.class));
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

            // when
            pointHistoryAdminService.rejectPointHistory(pointHistoryId);

            // then
            verify(pointHistoryAdminDomainService).rejectPointHistory(pointHistoryId);
            verify(userDomainService, never()).chargeUserPoint(any(UserPointChargeCommand.class));
        }
    }
}
