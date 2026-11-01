package br.com.nuvola.portability.operation;

import br.com.nuvola.portability.config.DynamoTables;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Repository
public class IdempotencyRepository {

	private final DynamoDbTable<IdempotencyRecord> table;

	public IdempotencyRepository(DynamoDbEnhancedClient enhancedClient) {
		this.table = enhancedClient.table(DynamoTables.IDEMPOTENCY, TableSchema.fromBean(IdempotencyRecord.class));
	}

	public boolean reserve(IdempotencyRecord record) {
		PutItemEnhancedRequest<IdempotencyRecord> request = PutItemEnhancedRequest.builder(IdempotencyRecord.class)
				.item(record)
				.conditionExpression(Expression.builder()
						.expression("attribute_not_exists(chave)")
						.build())
				.build();
		try {
			table.putItem(request);
			return true;
		} catch (ConditionalCheckFailedException e) {
			return false;
		}
	}

	public Optional<IdempotencyRecord> findByKey(String key) {
		return Optional.ofNullable(table.getItem(Key.builder().partitionValue(key).build()));
	}

}
