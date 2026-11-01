package br.com.nuvola.portability.config;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nuvola.portability.authorization.Authorization;
import br.com.nuvola.portability.authorization.AuthorizationRepository;
import br.com.nuvola.portability.operation.DestinationAccount;
import br.com.nuvola.portability.operation.Event;
import br.com.nuvola.portability.operation.IdempotencyRecord;
import br.com.nuvola.portability.operation.IdempotencyRepository;
import br.com.nuvola.portability.operation.Installment;
import br.com.nuvola.portability.operation.Operation;
import br.com.nuvola.portability.operation.OperationRepository;
import br.com.nuvola.portability.operation.OperationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.dynamo.create-tables=true")
@EnabledIfSystemProperty(named = "dynamo.local", matches = "true")
class DynamoRepositoriesTest {

	@Autowired
	private AuthorizationRepository authorizationRepository;

	@Autowired
	private OperationRepository operationRepository;

	@Autowired
	private IdempotencyRepository idempotencyRepository;

	@Test
	void savesAndFindsAuthorization() {
		authorizationRepository.save(new Authorization("BEN001", UUID.randomUUID().toString(), Instant.now()));

		assertThat(authorizationRepository.findByBeneficiaryId("BEN001")).isPresent();
		assertThat(authorizationRepository.findByBeneficiaryId("BEN999")).isEmpty();
	}

	@Test
	void savesOperationWithNestedDataAndFiltersByStatus() {
		Operation pending = sampleOperation("demo-pendente", "BEN001", OperationStatus.PENDENTE);
		Operation confirmed = sampleOperation("demo-confirmada", "BEN002", OperationStatus.CONFIRMADA);
		operationRepository.save(pending);
		operationRepository.save(confirmed);

		Operation found = operationRepository.findById("demo-pendente").orElseThrow();
		assertThat(found.getInstallments()).hasSize(2);
		assertThat(found.getEvents()).hasSize(1);
		assertThat(found.getDestinationAccount().getBank()).isEqualTo("001");
		assertThat(found.getTotalAmount()).isEqualByComparingTo("300.00");

		List<Operation> pendingOnly = operationRepository.findAll(OperationStatus.PENDENTE, null);
		assertThat(pendingOnly).extracting(Operation::getOperationId).contains("demo-pendente")
				.doesNotContain("demo-confirmada");

		List<Operation> fromBen002 = operationRepository.findAll(null, "BEN002");
		assertThat(fromBen002).extracting(Operation::getOperationId).contains("demo-confirmada")
				.doesNotContain("demo-pendente");
	}

	@Test
	void reservesIdempotencyKeyOnlyOnce() {
		String key = "demo-" + UUID.randomUUID();
		IdempotencyRecord record = new IdempotencyRecord(key, "demo-pendente", "hash-exemplo", Instant.now());

		assertThat(idempotencyRepository.reserve(record)).isTrue();
		assertThat(idempotencyRepository.reserve(record)).isFalse();
		assertThat(idempotencyRepository.findByKey(key)).isPresent();
	}

	private Operation sampleOperation(String operationId, String beneficiaryId, OperationStatus status) {
		Instant now = Instant.now();
		Operation operation = new Operation();
		operation.setOperationId(operationId);
		operation.setBeneficiaryId(beneficiaryId);
		operation.setCentralProtocol(UUID.randomUUID().toString());
		operation.setStatus(status);
		operation.setInstallments(List.of(
				new Installment("P001", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 1, 10), "ABERTA"),
				new Installment("P002", "CONV01", new BigDecimal("150.00"), LocalDate.of(2026, 2, 10), "ABERTA")));
		operation.setDestinationAccount(new DestinationAccount("001", "1234", "56789-0"));
		operation.setTotalAmount(new BigDecimal("300.00"));
		operation.setIdempotencyKey("demo-key-" + operationId);
		operation.setCreatedAt(now);
		operation.setUpdatedAt(now);
		operation.setEvents(List.of(new Event(status, now, "Exemplo gravado pelo teste")));
		return operation;
	}

}
