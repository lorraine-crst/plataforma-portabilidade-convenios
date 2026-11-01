package br.com.nuvola.portability.operation;

import br.com.nuvola.portability.schedule.InstallmentResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OperationResponse(
        String operacaoId,
        String beneficiarioId,
        OperationStatus status,
        String motivoRejeicao,
        Instant criadaEm,
        BigDecimal valorTotal,
        List<InstallmentResponse> parcelas,
        DestinationAccountResponse contaDestino,
        List<EventResponse> eventos) {

    public static OperationResponse from(Operation operation) {
        return new OperationResponse(
                operation.getOperationId(),
                operation.getBeneficiaryId(),
                operation.getStatus().publicStatus(),
                operation.getRejectionReason(),
                operation.getCreatedAt(),
                operation.getTotalAmount(),
                operation.getInstallments().stream().map(InstallmentResponse::from).toList(),
                DestinationAccountResponse.from(operation.getDestinationAccount()),
                operation.getEvents().stream().map(EventResponse::from).toList());
    }

}
