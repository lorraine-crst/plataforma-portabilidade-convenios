package br.com.nuvola.portability.operation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationSummaryResponseTest {

    @Test
    void countsKnownListCorrectly() {
        List<Operation> operations = List.of(
                operation(OperationStatus.PENDENTE, "300.00"),
                operation(OperationStatus.ENVIO_INCERTO, "100.00"),
                operation(OperationStatus.CONFIRMADA, "300.00"),
                operation(OperationStatus.CONFIRMADA, "150.00"),
                operation(OperationStatus.REJEITADA, "300.00"));

        OperationSummaryResponse summary = OperationSummaryResponse.from(operations);

        assertThat(summary.total()).isEqualTo(5);
        assertThat(summary.pendentes()).isEqualTo(2);
        assertThat(summary.confirmadas()).isEqualTo(2);
        assertThat(summary.rejeitadas()).isEqualTo(1);
        assertThat(summary.valorTotalConfirmado()).isEqualByComparingTo("450.00");
    }

    @Test
    void returnsZerosForEmptyList() {
        OperationSummaryResponse summary = OperationSummaryResponse.from(List.of());

        assertThat(summary.total()).isZero();
        assertThat(summary.pendentes()).isZero();
        assertThat(summary.valorTotalConfirmado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Operation operation(OperationStatus status, String totalAmount) {
        Operation operation = new Operation();
        operation.setTotalAmount(new BigDecimal(totalAmount));
        operation.changeStatus(status, "evento de teste");
        return operation;
    }

}
