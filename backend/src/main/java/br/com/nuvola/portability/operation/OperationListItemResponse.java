package br.com.nuvola.portability.operation;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationListItemResponse(
        String operacaoId,
        String beneficiarioId,
        OperationStatus status,
        String motivoRejeicao,
        Instant criadaEm,
        int quantidadeParcelas,
        BigDecimal valorTotal) {

    public static OperationListItemResponse from(Operation operation) {
        return new OperationListItemResponse(
                operation.getOperationId(),
                operation.getBeneficiaryId(),
                operation.getStatus().publicStatus(),
                operation.getRejectionReason(),
                operation.getCreatedAt(),
                operation.getInstallments().size(),
                operation.getTotalAmount());
    }

}
