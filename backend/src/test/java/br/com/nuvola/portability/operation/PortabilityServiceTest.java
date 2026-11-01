package br.com.nuvola.portability.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralInstallment;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.IdempotencyConflictException;
import br.com.nuvola.portability.error.InvalidInstallmentException;
import br.com.nuvola.portability.error.UncertainSubmissionException;
import br.com.nuvola.portability.schedule.ScheduleService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortabilityServiceTest {

    private static final String KEY = "KEY-1";

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private CentralClient centralClient;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private PortabilityService service;

    @Test
    void newKeyEndsPendingWithTwoEvents() {
        givenNewKey();
        when(centralClient.createOperation(any())).thenReturn("PROTO-1");

        Operation operation = service.create(KEY, request("P001", "P002"));

        assertThat(operation.getStatus()).isEqualTo(OperationStatus.PENDENTE);
        assertThat(operation.getCentralProtocol()).isEqualTo("PROTO-1");
        assertThat(operation.getEvents())
                .extracting(Event::getStatus)
                .containsExactly(OperationStatus.ENVIANDO, OperationStatus.PENDENTE);
        assertThat(operation.getTotalAmount()).isEqualByComparingTo("300.00");
        assertThat(operation.getInstallments()).hasSize(2);
    }

    @Test
    void repeatedKeyReturnsSameOperationWithoutCallingCentral() {
        PortabilityRequest request = request("P001");
        Operation existing = operation("OP-1", OperationStatus.ENVIANDO, OperationStatus.PENDENTE);
        givenExistingKey("OP-1", RequestFingerprint.of(request));
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(existing));

        Operation result = service.create(KEY, request);

        assertThat(result).isSameAs(existing);
        verify(centralClient, never()).createOperation(any());
        verify(scheduleService, never()).findSchedule(anyString());
    }

    @Test
    void sameKeyWithDifferentDataIsConflict() {
        givenExistingKey("OP-1", "hash-de-outro-pedido");

        assertThatThrownBy(() -> service.create(KEY, request("P001")))
                .isInstanceOf(IdempotencyConflictException.class);
        verify(centralClient, never()).createOperation(any());
    }

    @Test
    void retryAfterSendFailureCallsCentralAgain() {
        PortabilityRequest request = request("P001");
        Operation failed = operation("OP-1", OperationStatus.ENVIANDO, OperationStatus.FALHA_ENVIO);
        givenExistingKey("OP-1", RequestFingerprint.of(request));
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(failed));
        when(centralClient.createOperation(any())).thenReturn("PROTO-2");

        Operation result = service.create(KEY, request);

        verify(centralClient, times(1)).createOperation(any());
        assertThat(result.getStatus()).isEqualTo(OperationStatus.PENDENTE);
        assertThat(result.getEvents())
                .extracting(Event::getStatus)
                .containsExactly(OperationStatus.ENVIANDO, OperationStatus.FALHA_ENVIO,
                        OperationStatus.ENVIANDO, OperationStatus.PENDENTE);
    }

    @Test
    void rejectsInstallmentMissingFromSchedule() {
        when(idempotencyRepository.findByKey(KEY)).thenReturn(Optional.empty());
        when(scheduleService.findSchedule("BEN001")).thenReturn(schedule());

        assertThatThrownBy(() -> service.create(KEY, request("P999")))
                .isInstanceOf(InvalidInstallmentException.class)
                .hasMessageContaining("P999");
        verify(operationRepository, never()).save(any());
        verify(idempotencyRepository, never()).reserve(any());
        verify(centralClient, never()).createOperation(any());
    }

    @Test
    void rejectsInstallmentThatIsNotOpen() {
        when(idempotencyRepository.findByKey(KEY)).thenReturn(Optional.empty());
        when(scheduleService.findSchedule("BEN001")).thenReturn(schedule());

        assertThatThrownBy(() -> service.create(KEY, request("P003")))
                .isInstanceOf(InvalidInstallmentException.class)
                .hasMessageContaining("QUITADA");
        verify(centralClient, never()).createOperation(any());
    }

    @Test
    void marksSendFailureWhenCentralIsUnavailable() {
        givenNewKey();
        when(centralClient.createOperation(any())).thenThrow(new CentralUnavailableException());

        assertThatThrownBy(() -> service.create(KEY, request("P001")))
                .isInstanceOf(CentralUnavailableException.class);
        verify(operationRepository, times(2))
                .save(argThat(operation -> operation.getStatus() == OperationStatus.FALHA_ENVIO));
    }

    @Test
    void marksUncertainSubmissionOnTimeout() {
        givenNewKey();
        when(centralClient.createOperation(any())).thenThrow(new UncertainSubmissionException());

        assertThatThrownBy(() -> service.create(KEY, request("P001")))
                .isInstanceOf(UncertainSubmissionException.class);
        verify(operationRepository, times(2))
                .save(argThat(operation -> operation.getStatus() == OperationStatus.ENVIO_INCERTO));
    }

    @Test
    void losingConcurrentRequestDeletesItsOperationAndReturnsTheWinner() {
        PortabilityRequest request = request("P001");
        Operation winner = operation("OP-WINNER", OperationStatus.ENVIANDO, OperationStatus.PENDENTE);
        when(idempotencyRepository.findByKey(KEY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional
                        .of(new IdempotencyRecord(KEY, "OP-WINNER", RequestFingerprint.of(request), Instant.now())));
        when(scheduleService.findSchedule("BEN001")).thenReturn(schedule());
        when(idempotencyRepository.reserve(any())).thenReturn(false);
        when(operationRepository.findById("OP-WINNER")).thenReturn(Optional.of(winner));

        Operation result = service.create(KEY, request);

        assertThat(result).isSameAs(winner);
        verify(operationRepository).delete(anyString());
        verify(centralClient, never()).createOperation(any());
    }

    private void givenNewKey() {
        when(idempotencyRepository.findByKey(KEY)).thenReturn(Optional.empty());
        when(scheduleService.findSchedule("BEN001")).thenReturn(schedule());
        when(idempotencyRepository.reserve(any())).thenReturn(true);
    }

    private void givenExistingKey(String operationId, String requestHash) {
        when(idempotencyRepository.findByKey(KEY))
                .thenReturn(Optional.of(new IdempotencyRecord(KEY, operationId, requestHash, Instant.now())));
    }

    private PortabilityRequest request(String... installmentIds) {
        return new PortabilityRequest("BEN001", List.of(installmentIds),
                new DestinationAccountRequest("001", "1234", "56789-0"));
    }

    private List<CentralInstallment> schedule() {
        return List.of(
                new CentralInstallment("P001", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 1, 10), "ABERTA"),
                new CentralInstallment("P002", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 2, 10), "ABERTA"),
                new CentralInstallment("P003", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 3, 10),
                        "QUITADA"));
    }

    private Operation operation(String operationId, OperationStatus... history) {
        Operation operation = new Operation();
        operation.setOperationId(operationId);
        operation.setBeneficiaryId("BEN001");
        operation.setInstallments(List.of(
                new Installment("P001", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 1, 10), "ABERTA")));
        operation.setDestinationAccount(new DestinationAccount("001", "1234", "56789-0"));
        for (OperationStatus status : history) {
            operation.changeStatus(status, "evento de teste");
        }
        return operation;
    }

}
