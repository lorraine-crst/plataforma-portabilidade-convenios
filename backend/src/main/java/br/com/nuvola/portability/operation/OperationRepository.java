package br.com.nuvola.portability.operation;

import br.com.nuvola.portability.config.DynamoTables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class OperationRepository {

	private final DynamoDbTable<Operation> table;

	public OperationRepository(DynamoDbEnhancedClient enhancedClient) {
		this.table = enhancedClient.table(DynamoTables.OPERATIONS, TableSchema.fromBean(Operation.class));
	}

	public void save(Operation operation) {
		table.putItem(operation);
	}

	public Optional<Operation> findById(String operationId) {
		Key key = Key.builder().partitionValue(operationId).build();
		return Optional.ofNullable(table.getItem(key));
	}

	public List<Operation> findAll(OperationStatus status, String beneficiaryId) {
		ScanEnhancedRequest.Builder request = ScanEnhancedRequest.builder();
		Expression filter = buildFilter(status, beneficiaryId);
		if (filter != null) {
			request.filterExpression(filter);
		}
		return table.scan(request.build()).items().stream().toList();
	}

	private Expression buildFilter(OperationStatus status, String beneficiaryId) {
		List<String> conditions = new ArrayList<>();
		Map<String, String> names = new HashMap<>();
		Map<String, AttributeValue> values = new HashMap<>();

		if (status != null) {
			conditions.add("#status = :status");
			names.put("#status", "status");
			values.put(":status", AttributeValue.fromS(status.name()));
		}
		if (beneficiaryId != null && !beneficiaryId.isBlank()) {
			conditions.add("#beneficiarioId = :beneficiarioId");
			names.put("#beneficiarioId", "beneficiarioId");
			values.put(":beneficiarioId", AttributeValue.fromS(beneficiaryId));
		}

		if (conditions.isEmpty()) {
			return null;
		}
		return Expression.builder()
				.expression(String.join(" AND ", conditions))
				.expressionNames(names)
				.expressionValues(values)
				.build();
	}

}