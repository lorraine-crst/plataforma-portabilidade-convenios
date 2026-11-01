package br.com.nuvola.portability.operation;

import java.math.BigDecimal;
import java.util.List;

public record OperationSummaryResponse(
        long total,
        long pendentes,
        long confirmadas,
        long rejeitadas,
        BigDecimal valorTotalConfirmado) {

    public static OperationSummaryResponse from(List<Operation> operations) {
        BigDecimal confirmedAmount = operations.stream()
                .filter(operation -> hasPublicStatus(operation, OperationStatus.CONFIRMADA))
                .map(Operation::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OperationSummaryResponse(
                operations.size(),
                count(operations, OperationStatus.PENDENTE),
                count(operations, OperationStatus.CONFIRMADA),
                count(operations, OperationStatus.REJEITADA),
                confirmedAmount);
    }

    private static long count(List<Operation> operations, OperationStatus status) {
        return operations.stream()
                .filter(operation -> hasPublicStatus(operation, status))
                .count();
    }

    private static boolean hasPublicStatus(Operation operation, OperationStatus status) {
        return operation.getStatus().publicStatus() == status;
    }

}
