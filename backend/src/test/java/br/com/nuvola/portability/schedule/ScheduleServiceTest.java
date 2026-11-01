package br.com.nuvola.portability.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nuvola.portability.authorization.AuthorizationService;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralInstallment;
import br.com.nuvola.portability.error.BeneficiaryNotAuthorizedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private CentralClient centralClient;

    @InjectMocks
    private ScheduleService service;

    @Test
    void doesNotCallCentralWhenBeneficiaryHasNotAuthorized() {
        doThrow(new BeneficiaryNotAuthorizedException("BEN003")).when(authorizationService).ensureAuthorized("BEN003");

        assertThatThrownBy(() -> service.findSchedule("BEN003"))
                .isInstanceOf(BeneficiaryNotAuthorizedException.class);
        verify(centralClient, never()).getSchedule(anyString());
    }

    @Test
    void returnsCentralScheduleWhenBeneficiaryIsAuthorized() {
        List<CentralInstallment> schedule = List.of(
                new CentralInstallment("P001", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 1, 10), "ABERTA"));
        when(centralClient.getSchedule("BEN001")).thenReturn(schedule);

        assertThat(service.findSchedule("BEN001")).isEqualTo(schedule);
    }

}
