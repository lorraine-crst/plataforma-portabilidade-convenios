package br.com.nuvola.portability.operation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OperationTest {

	@Test
	void recordsOneEventPerStatusChangeInOrder() {
		Operation operation = new Operation();

		operation.changeStatus(OperationStatus.ENVIANDO, "Solicitação registrada");
		operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");
		operation.changeStatus(OperationStatus.CONFIRMADA, "Confirmada pela Central");

		assertThat(operation.getStatus()).isEqualTo(OperationStatus.CONFIRMADA);
		assertThat(operation.getEvents())
				.extracting(Event::getStatus)
				.containsExactly(OperationStatus.ENVIANDO, OperationStatus.PENDENTE, OperationStatus.CONFIRMADA);
		assertThat(operation.getUpdatedAt()).isEqualTo(operation.getEvents().getLast().getOccurredAt());
	}

	@Test
	void ignoresRepeatedStatus() {
		Operation operation = new Operation();
		operation.changeStatus(OperationStatus.PENDENTE, "Aceita pela Central");

		boolean changed = operation.changeStatus(OperationStatus.PENDENTE, "Ainda pendente");

		assertThat(changed).isFalse();
		assertThat(operation.getEvents()).hasSize(1);
		assertThat(operation.getEvents().getFirst().getDescription()).isEqualTo("Aceita pela Central");
	}

}
