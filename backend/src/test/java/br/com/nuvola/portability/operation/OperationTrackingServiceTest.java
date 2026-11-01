package br.com.nuvola.portability.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nuvola.portability.auth.CurrentUser;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.central.CentralOperationStatus;
import br.com.nuvola.portability.error.CentralUnavailableException;
import br.com.nuvola.portability.error.OperationAccessDeniedException;
import br.com.nuvola.portability.error.OperationNotFoundException;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
class OperationTrackingServiceTest {

    private static final String REASON = "Beneficiário atingiu o limite de margem consignável";

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private CentralClient centralClient;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private OperationTrackingService service;

    @Test
    void appliesRejectionFromCentralWithReasonAndNewEvent() {
        givenOperator();
        Operation operation = pendingOperation("OP-1", "BEN002");
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(operation));
        when(centralClient.getOperationStatus("PROTO-1"))
                .thenReturn(new CentralOperationStatus(OperationStatus.REJEITADA, REASON));

        Operation result = service.findById("OP-1");

        assertThat(result.getStatus()).isEqualTo(OperationStatus.REJEITADA);
        assertThat(result.getRejectionReason()).isEqualTo(REASON);
        assertThat(result.getEvents()).hasSize(3);
        assertThat(result.getEvents().getLast().getStatus()).isEqualTo(OperationStatus.REJEITADA);
        assertThat(result.getEvents().getLast().getDescription()).isEqualTo("Rejeitada pela Central");
        verify(operationRepository).save(operation);
    }

    @Test
    void keepsPendingWithoutSavingWhenCentralIsStillProcessing() {
        givenOperator();
        Operation operation = pendingOperation("OP-1", "BEN001");
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(operation));
        when(centralClient.getOperationStatus("PROTO-1"))
                .thenReturn(new CentralOperationStatus(OperationStatus.PENDENTE, null));

        Operation result = service.findById("OP-1");

        assertThat(result.getStatus()).isEqualTo(OperationStatus.PENDENTE);
        assertThat(result.getEvents()).hasSize(2);
        verify(operationRepository, never()).save(any());
    }

    @Test
    void doesNotQueryCentralWhenResultIsFinal() {
        givenOperator();
        Operation operation = pendingOperation("OP-1", "BEN001");
        operation.changeStatus(OperationStatus.CONFIRMADA, "Confirmada pela Central");
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(operation));

        service.findById("OP-1");

        verify(centralClient, never()).getOperationStatus(anyString());
    }

    @Test
    void returnsStoredOperationWhenCentralIsUnavailable() {
        givenOperator();
        Operation operation = pendingOperation("OP-1", "BEN001");
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(operation));
        when(centralClient.getOperationStatus("PROTO-1")).thenThrow(new CentralUnavailableException());

        Operation result = service.findById("OP-1");

        assertThat(result.getStatus()).isEqualTo(OperationStatus.PENDENTE);
        verify(operationRepository, never()).save(any());
    }

    @Test
    void forbidsBeneficiaryFromViewingAnotherBeneficiarysOperation() {
        givenBeneficiary("BEN001");
        when(operationRepository.findById("OP-2")).thenReturn(Optional.of(pendingOperation("OP-2", "BEN002")));

        assertThatThrownBy(() -> service.findById("OP-2"))
                .isInstanceOf(OperationAccessDeniedException.class);
        verify(centralClient, never()).getOperationStatus(anyString());
    }

    @Test
    void allowsBeneficiaryToViewOwnOperation() {
        givenBeneficiary("BEN001");
        when(operationRepository.findById("OP-1")).thenReturn(Optional.of(pendingOperation("OP-1", "BEN001")));
        when(centralClient.getOperationStatus("PROTO-1"))
                .thenReturn(new CentralOperationStatus(OperationStatus.CONFIRMADA, null));

        Operation result = service.findById("OP-1");

        assertThat(result.getStatus()).isEqualTo(OperationStatus.CONFIRMADA);
        assertThat(result.getRejectionReason()).isNull();
    }

    @Test
    void throwsNotFoundForUnknownOperation() {
        when(operationRepository.findById("OP-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("OP-X"))
                .isInstanceOf(OperationNotFoundException.class);
    }

    @Test
    void beneficiaryListAlwaysUsesUsernameFromToken() {
        givenBeneficiary("BEN001");
        when(operationRepository.findAll(null, "BEN001")).thenReturn(List.of());

        service.list(null, "BEN002");

        verify(operationRepository).findAll(null, "BEN001");
    }

    @Test
    void operatorListUsesRequestedBeneficiaryFilter() {
        givenOperator();
        when(operationRepository.findAll(null, "BEN002")).thenReturn(List.of());

        service.list(null, "BEN002");

        verify(operationRepository).findAll(null, "BEN002");
    }

    @Test
    void listRefreshesPendingExcludesSendFailuresAndSortsNewestFirst() {
        givenOperator();
        Operation older = operationWithStatus("OP-OLD", OperationStatus.CONFIRMADA, "2026-10-01T10:00:00Z");
        Operation failed = operationWithStatus("OP-FAIL", OperationStatus.FALHA_ENVIO, "2026-10-02T10:00:00Z");
        Operation newer = pendingOperation("OP-NEW", "BEN001");
        newer.setCreatedAt(Instant.parse("2026-10-03T10:00:00Z"));
        when(operationRepository.findAll(null, null)).thenReturn(List.of(older, failed, newer));
        when(centralClient.getOperationStatus("PROTO-1"))
                .thenReturn(new CentralOperationStatus(OperationStatus.CONFIRMADA, null));

        List<Operation> result = service.list(OperationStatus.CONFIRMADA, null);

        assertThat(result).extracting(Operation::getOperationId).containsExactly("OP-NEW", "OP-OLD");
    }

    private void givenOperator() {
        when(currentUser.isOperator()).thenReturn(true);
    }

    private void givenBeneficiary(String username) {
        when(currentUser.isOperator()).thenReturn(false);
        when(currentUser.username()).thenReturn(username);
    }

    private Operation pendingOperation(String operationId, String beneficiaryId) {
        Operation operation = new Operation();
        operation.setOperationId(operationId);
        operation.setBeneficiaryId(beneficiaryId);
        operation.setCentralProtocol("PROTO-1");
        operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
        operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");
        return operation;
    }

    private Operation operationWithStatus(String operationId, OperationStatus status, String createdAt) {
        Operation operation = new Operation();
        operation.setOperationId(operationId);
        operation.setBeneficiaryId("BEN001");
        operation.setCreatedAt(Instant.parse(createdAt));
        operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
        operation.changeStatus(status, "evento de teste");
        return operation;
    }

}
